package com.wcb.cms.modelmaker.brain.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wcb.cms.modelmaker.api.CMSEntityEntry;
import com.wcb.cms.modelmaker.api.CMSRoseModellingResult;
/**
 * A helper class to stitch the end product of all bundle
 * objects produced.
 */
public final class RoseModellingResultImpl implements CMSRoseModellingResult {

	private static final String STAR = "*";
	private String dynamicSql;
	private final Map<String, String> inputStruct;
	private final Map<String, String> outputStruct;

	public RoseModellingResultImpl(String selectQuery,
			List<CMSEntityEntry> intoClauseList,
			List<CMSEntityEntry> constAndVarialbeList) {
		CMSEntityEntry.resetVariableSuffix();
		composeCuramNonStandardSelectQuery(selectQuery, intoClauseList, constAndVarialbeList);
		inputStruct = new Hashtable<>(constAndVarialbeList.size());
		composeStruct(constAndVarialbeList, inputStruct);
		outputStruct = new Hashtable<>(intoClauseList.size());
		composeStruct(intoClauseList, outputStruct);
	}

	private String addConstantVarialbePlaceHolderIn(String sqlQuery, List<CMSEntityEntry> constAndVarialbeMetaDataList) {
		for (CMSEntityEntry entry : constAndVarialbeMetaDataList) {
			String replacement = entry.getSqlElement();
			if(replacement.matches(".+'\\w+'.*")){
				Matcher theMatcher = Pattern.compile("'\\w+'").matcher(replacement);
				while(theMatcher.find()){
					String group = theMatcher.group();
					String variable = entry.getEntityAttribute()+"_"+group.replaceAll("\\W", "");
					//UPDATE
					entry.addToVariableAttrMap(variable, entry.getEntityAttribute());
					replacement = replacement.replace(group,
							":" + entry.getVariable(variable));
				}
				//REPLACE
				sqlQuery = sqlQuery.replaceFirst(
						entry.getSqlElement().replace("(", "\\(").replace(")", "\\)")
						, replacement);
			}else{
				String variable = replacement.replaceFirst(".+:", "");
				//UPDATE
				entry.addToVariableAttrMap(variable, entry.getEntityAttribute());
				//REPLACE
				sqlQuery = sqlQuery.replaceFirst(replacement,
						replacement.replaceFirst(":.+", ":"+entry.getVariable(variable)));
			}
		}
		return sqlQuery;
	}

	private String addIntoPlaceHolderIn(String sqlQuery, List<CMSEntityEntry> intoStatementMetaDataList) {
		for (CMSEntityEntry entry : intoStatementMetaDataList) {
			if(entry.getSqlElement().endsWith(STAR)){
				Set<String> entityAttrs = entry.getEntityAttrSet();
				StringWriter out = new StringWriter();
				PrintWriter pw = new PrintWriter(out);//pw can do println, so I don't have to hardcode this "\n".
				for (String entityAttr : entityAttrs) {
					String[] split = entityAttr.split("\\.");//e.g. claimcycle.claimcycleid

					//UPDATE
					entry.addToVariableAttrMap(split[1], entityAttr);//so we will have attr_a mapping to attr
					String tableAlias = entry.getTableAlias(split[0].toUpperCase());
					pw.println(tableAlias+"."+split[1]
							+"{"+entry.getVariable(split[1])+"}" +",");//e.g.: cc.claimcycleid{claimcycleid_a}
				}
				//REPLACE
				if(entry.getSqlElement().equals("*") == false){
					sqlQuery = sqlQuery.replace(entry.getSqlElement(), "@");
				}
				sqlQuery = sqlQuery.replaceFirst("[*@]", out.toString() + "@");
				pw.close();
			}else{
				String variable = entry.getColumnAlias().isEmpty() ? entry.getEntityAttribute() : entry.getColumnAlias();
				//UPDATE
				entry.addToVariableAttrMap(variable, entry.getEntityAttribute());
				//REPALCE
				sqlQuery = sqlQuery.replaceFirst(entry.getSqlElement(),
						entry.getSqlElement()
						+ "{" + entry.getVariable(variable)
						+ "}");
			}
		}
		StringWriter out = new StringWriter();
		PrintWriter pw = new PrintWriter(out);//pw can do println, so I don't have to hardcode this "\n".

		int indexToInsertIntoClause = sqlQuery.lastIndexOf("}") + 1;
		pw.println(sqlQuery.substring(0, indexToInsertIntoClause)); //SELECT CLAUSE
		pw.println("INTO");
		Pattern pattern = Pattern.compile("\\{\\w+}");
		Matcher matcher = pattern.matcher(sqlQuery);
		while(matcher.find()){
			pw.println(":"+matcher.group().replaceAll("[\\{}]", "") + ",");
		}
		pw.println("<FROM>");
		String finalQuery = out.toString().replaceAll("\\{\\w+}", "");
		pw.close();

		finalQuery = finalQuery.substring(0, finalQuery.lastIndexOf(","))//trim the last ",";
				+ finalQuery.substring(finalQuery.lastIndexOf(",")+1);//keep the FROM and the "\r\n" around it
		String fromClause = sqlQuery.substring(indexToInsertIntoClause).replaceFirst("(F|f)(R|r)(O|o)(M|m)", "FROM");
		fromClause = fromClause.substring(fromClause.indexOf("FROM"));//From clause
		finalQuery = finalQuery.replace("<FROM>", fromClause).trim();
		return finalQuery;
	}

	/**
	 * Updates variable field in the list of CMSEntityEntry and set the local field
	 * @param sqlQuery
	 * @param intoStatementMetaDataList
	 * @param constAndVarialbeMetaDataList
	 */
	private void composeCuramNonStandardSelectQuery(String sqlQuery,
			List<CMSEntityEntry> intoStatementMetaDataList,
			List<CMSEntityEntry> constAndVarialbeMetaDataList) {
		sqlQuery = addIntoPlaceHolderIn(sqlQuery, intoStatementMetaDataList);
		sqlQuery = addConstantVarialbePlaceHolderIn(sqlQuery, constAndVarialbeMetaDataList);
		this.dynamicSql = sqlQuery;
	}

	private void composeStruct(
			List<CMSEntityEntry> list, Map<String, String> strutMap) {
		for (CMSEntityEntry entry : list) {
			Set<String> variableList = entry.getVariableSet();
			for (String variable : variableList) {
				strutMap.put(variable, entry.getDomainDefinition(entry.getAttribute(variable)));
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.wcb.cms.modelmaker.api.CMSRoseModellingResult#getCuramNonStandardSelectQuery()
	 */
	@Override
	public String getCuramNonStandardSelectQuery() {
		return this.dynamicSql;
	}

	/* (non-Javadoc)
	 * @see com.wcb.cms.modelmaker.api.CMSRoseModellingResult#getInputStruct()
	 */
	@Override
	public Map<String, String> getInputStruct() {
		return this.inputStruct;
	}

	/* (non-Javadoc)
	 * @see com.wcb.cms.modelmaker.api.CMSRoseModellingResult#getOutputStruct()
	 */
	@Override
	public Map<String, String> getOutputStruct() {
		return this.outputStruct;
	}

}
