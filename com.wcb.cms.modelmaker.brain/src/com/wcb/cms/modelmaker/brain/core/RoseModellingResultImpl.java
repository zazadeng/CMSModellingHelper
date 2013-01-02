package com.wcb.cms.modelmaker.brain.core;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wcb.cms.modelmaker.api.CMSEntityEntry;
import com.wcb.cms.modelmaker.api.CMSRoseModellingResult;

public final class RoseModellingResultImpl implements CMSRoseModellingResult {

	private String dynamicSql;
	private Map<String, String> inputStruct;
	private Map<String, String> outputStruct;

	public RoseModellingResultImpl(String selectQuery,
			List<CMSEntityEntry> intoClauseList,
			List<CMSEntityEntry> constAndVarialbeList) {
		CMSEntityEntry.resetVariableSuffix();
		composeCuramNonStandardSelectQuery(selectQuery, intoClauseList, constAndVarialbeList);
		composeInputStruct(constAndVarialbeList);
		composeOutputStruct(intoClauseList);
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

		for (CMSEntityEntry entry : intoStatementMetaDataList) {
			String variable = entry.getColumnAlias().isEmpty() ? entry.getAttribute() : entry.getColumnAlias();
			//UPDATE
			entry.addVariable(variable);
			//REPALCE
			sqlQuery = sqlQuery.replaceFirst(entry.getSqlElement(),
					entry.getSqlElement()
					+ "{" + entry.getVariable(variable)
					+ "}");

		}
		int indexToInsert = sqlQuery.lastIndexOf("}") + 1;
		Pattern pattern = Pattern.compile("\\{\\w+}");
		Matcher matcher = pattern.matcher(sqlQuery);
		String intoString = "INTO";
		while(matcher.find()){
			intoString += matcher.group().replaceFirst("\\{", ",:").replaceFirst("}", " \r\n");
		}
		intoString = intoString.replaceFirst("INTO,", "\r\n INTO \r\n");
		sqlQuery = sqlQuery.substring(0, indexToInsert)
				+ intoString + sqlQuery.substring(indexToInsert);
		sqlQuery = sqlQuery.replaceAll("\\{\\w+}", "");

		for (CMSEntityEntry entry : constAndVarialbeMetaDataList) {
			String replacement = entry.getSqlElement();
			if(replacement.matches(".+'\\w+'.*")){
				Matcher theMatcher = Pattern.compile("'\\w+'").matcher(replacement);
				while(theMatcher.find()){
					String group = theMatcher.group();
					String variable = entry.getAttribute()+"_"+group.replaceAll("\\W", "");
					//UPDATE
					entry.addVariable(variable);
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
				entry.addVariable(variable);
				//REPLACE
				sqlQuery = sqlQuery.replaceFirst(replacement,
						replacement.replaceFirst(":.+", ":"+entry.getVariable(variable)));
			}
		}
		this.dynamicSql = sqlQuery;
	}

	private void composeInputStruct(
			List<CMSEntityEntry> input) {
		if(this.inputStruct == null){
			this.inputStruct = new Hashtable<>(input.size());//Could be more, we have a IN predicate, but probability is lower for IN
		}
		for (CMSEntityEntry entry : input) {
			List<String> variableList = entry.getVariableList();
			for (String variable : variableList) {
				inputStruct.put(variable, entry.getDomainDefinition());
			}
		}

	}

	private void composeOutputStruct(
			List<CMSEntityEntry> output) {
		if(this.outputStruct == null){
			this.outputStruct = new Hashtable<>(output.size());
		}
		for (CMSEntityEntry entry : output) {
			List<String> variableList = entry.getVariableList();
			for (String variable : variableList) {
				outputStruct.put(variable, entry.getDomainDefinition());
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
