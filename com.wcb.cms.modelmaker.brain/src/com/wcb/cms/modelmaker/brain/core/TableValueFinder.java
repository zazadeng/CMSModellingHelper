package com.wcb.cms.modelmaker.brain.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wcb.cms.modelmaker.api.CMSEntityDtlsDB;
import com.wcb.cms.modelmaker.api.ColumnInSQL;
import com.wcb.cms.modelmaker.api.SelectQueryReader;
import com.wcb.cms.modelmaker.api.TableInSQL;

public class TableValueFinder {

	private static final String kColon = ":";
	private static final String kEquals = "=";
	private static final String kNotEquals = "<>";
	private static final String kDomainFollows = "@";
	private static final String kColumnAlaisOrTableAlaisFollows = "_";
	private static final String kInvalidAttribute = "INVALID_ATTRIBUTE";
	
	
	final private CMSEntityDtlsDB dbConnection;
	final private Set<String> tableIndexes;
	final private String tableName;
	//private SelectQueryReader selectQueryReader;
	//private String selectSql;
	
	
	public TableValueFinder(CMSEntityDtlsDB dbc, 
							String tableName, 
							Set<String> tableIndexes
							//SelectQueryReader selectQueryReader,
							//String selectQuery
							) throws Exception {
		dbConnection = dbc;
		this.tableName = tableName;
		this.tableIndexes  = tableIndexes;
		//selectQueryReader = new SelectQueryReaderImpl();
		//selectQueryReader = new SelectQueryReaderDTPPlugInImpl();
		//this.selectQueryReader = selectQueryReader;
		//selectSql = getValidatedSelectStatementString(selectQuery);
	}
	
	

	/*public String validateTableName(Set<TableInSQL> tableNameSet) throws Exception {
		for (Iterator<TableInSQL> iterator = tableNameSet.iterator(); iterator.hasNext();) {
			TableInSQL entityName = iterator.next();
			List<String> records = dbConnection.getRecords(table, new Object[]{entityName.getName()});
			if(records.size() == 0){
				throw new Exception("\"" + entityName +"\" can not be found.");
			}
		}
		return null;//TODO: has to return null?
	}*/

	private void checkIfTableAliasAreUnique(Set<TableInSQL> tableSet) throws TableAliasNotUniqueException {
		//something like "tableA" and "tableB" have the same alias named "C"
		Set<TableInSQL> tmp =  new HashSet<TableInSQL>();
		tmp.addAll(tableSet);
		for (Iterator<TableInSQL> iterator = tmp.iterator(); iterator.hasNext();) {
			TableInSQL table = iterator.next();
			for (Iterator<TableInSQL> iteratorTwo = tmp.iterator(); iteratorTwo.hasNext();) {
				TableInSQL tmpTable = iteratorTwo.next();
				if(table.getName().equals(tmpTable.getName())){
					continue;
				}
				String alias = table.getAlias();
				if((alias != null) && alias.equals(tmpTable.getAlias())){
					throw new TableAliasNotUniqueException(table.getName(),
															tmpTable.getName(),
															alias);
				}
			}
		}
	}

	private String getColumnNameAndDomanDef(String tableNameAlias, String columnName, Set<TableInSQL> tableSet) throws Exception{
		List<String> dbRecord = null;
		
		for (Iterator<TableInSQL> iterator = tableSet.iterator(); iterator.hasNext();) {
			TableInSQL table = iterator.next();
			if (((table.getAlias() != null) && !table.getAlias().equals(""))
					&& table.getAlias().equalsIgnoreCase(
							tableNameAlias)) {
				dbRecord = this.getRecords( new Object[]{table.getName(), columnName});
				try{
					String[] values = dbRecord.get(0).split(kColon);
					return values[1]+kEquals+values[2];
				}catch (java.lang.IndexOutOfBoundsException e){
					throw new Exception ("column \""+ columnName + "\" is NOT in table \""+table.getName()+"\"");
				}
				
			}
		}
		return kInvalidAttribute;
	}

	public final String getConstantsPlaceHolders(final SelectQueryReader selectQueryReader, String selectQuery) throws Exception {
		
		final String selectSql = selectQueryReader.getParsedSQL(selectQuery);
		String resultingSql = selectSql;
		final Set<TableInSQL> tableSet = selectQueryReader.getTableSet(selectSql);
		/*
		 * e.g.:
		 * CC.CLAIMCYCLEID = :ccID
		 * CC.RECORDSTATUS = 'RST1'
		 */
		List<String>  equalToStringValueExprssionList = selectQueryReader.getEqualToStringValueExprssionList(selectSql);
		for (Iterator<String> iterator = equalToStringValueExprssionList.iterator(); iterator.hasNext();) {
			
			String binaryExpString =  iterator.next();
		
			String[] elements = binaryExpString.split(kEquals+"|"+kNotEquals);// vrc.retrBnftSttsTypeCd  = 'RBS1', vrc.retrBnftSttsTypeCd  <> 'RBS1'
			
			ColumnInSQL dbColumn = new ColumnInSQL();
			String column = elements[0].trim();
			if(column.contains(".")){
				String[] values = column.split("\\.");
				//dbColumn = new ColumnInSQL(values[0], values[1]);
				dbColumn.tableAlias(values[0]).columnName(values[1]);
			}else{
				dbColumn.columnName(column);
			}
			if(dbColumn.getTableShortName().trim().isEmpty() && (tableSet.size() == 1)){
				//there is only one element in the top level FROM clause
				dbColumn.tableAlias(tableSet.iterator().next().getAlias());
			}
			String columnNameAndDomanDef = getColumnNameAndDomanDef(dbColumn.getTableShortName(), dbColumn.getColumnName(), tableSet);
			String[] values = columnNameAndDomanDef.split(kEquals);
			String stringValue =elements[1].replaceAll("\\s", "");
			if(stringValue.startsWith(kColon)){
				stringValue += "INPUT";
			}else{
				stringValue = kColon + values[0] +
						stringValue.replaceAll("'", "");
			}
			stringValue = stringValue + kColumnAlaisOrTableAlaisFollows +
							dbColumn.getTableShortName().toUpperCase() + kDomainFollows +
							values[1];
			resultingSql = resultingSql.replace(binaryExpString, binaryExpString.substring(0, binaryExpString.indexOf(elements[1]))+stringValue);
		}
		
		/*
		 * e.g.:
		 * CC.CLAIMCYCLETYPECD IN ('TT1', 'TT2')
		 */
		List<String> inStringValueExprssionList = selectQueryReader.getInStringValueExprssionList(selectSql);
		for (Iterator<String> iterator = inStringValueExprssionList.iterator(); iterator.hasNext();) {
			String inExpString =  iterator.next();
			String[] elements = inExpString.split("IN");
			ColumnInSQL dbColumn = new ColumnInSQL();
			if(elements[0].contains(".")){
				String[] elems = elements[0].trim().split("\\.");
				dbColumn.tableAlias(elems[0]).columnName(elems[1]);
			}else{
				dbColumn.columnName(elements[0].trim());
			}
			if(dbColumn.getTableShortName().trim().isEmpty() && (tableSet.size() == 1)){
				//there is only one element in the top level FROM clause
				dbColumn.tableAlias(tableSet.iterator().next().getAlias());
			}
			
			String columnNameAndDomanDef = getColumnNameAndDomanDef(dbColumn.getTableShortName(), dbColumn.getColumnName(), tableSet);
			String[] values = columnNameAndDomanDef.split(kEquals);
			String[] stringValues =  elements[1].replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("'", "").split(",");
			String rightExp = "(";
			String tmp = "";
			for (int i = 0; i < stringValues.length; i++) {
				tmp = kColon + values[0] + stringValues[i].trim() + 
						kColumnAlaisOrTableAlaisFollows + dbColumn.getTableShortName().toUpperCase() +
							kDomainFollows + values[1];
				rightExp =  rightExp + ", " + tmp;
			}
			rightExp = rightExp.replaceFirst(", ", "") +")";
			resultingSql = resultingSql.replace(inExpString, elements[0].trim()+" IN "+rightExp);
			//System.out.println(rightExp);
		}
		/*
		 * e.g.:
		 * :tDate BETWEEN CPET.EFFDT AND COALESCE(CPET.EXPDT, '9999-12-31')
		 * current date BETWEEN :tDate AND COALESCE(CPET.EXPDT, '9999-12-31')
		 * current date BETWEEN CPET.EFFDT AND COALESCE(:tDate, '9999-12-31')
		 * 
		 */
		List<String> betweenStringValueExprssionList = selectQueryReader.getBetweenStringValueExprssionList(selectSql);
		for (Iterator<String> iterator = betweenStringValueExprssionList.iterator(); iterator.hasNext();) {
			String betweenExpString =  iterator.next();
			Pattern pattern = Pattern.compile(":\\w+(\\s*)");
			Matcher matcher = pattern.matcher(betweenExpString); 
			final StringBuffer sb = new StringBuffer();
			while(matcher.find()){
				String unit = matcher.group();
				unit = unit.trim() + kDomainFollows + "CURAM_DATE"+" ";//TODO fix this hardcoded part
				matcher.appendReplacement(sb, unit);		
			}
			matcher.appendTail(sb);
			resultingSql = resultingSql.replace(betweenExpString, sb);
			
		}
		return resultingSql;//statement.toString().replaceAll("'*zaza'*", "");
	}

	private String getFullTableNameFromAlias(String AliasString, Set<TableInSQL> tableSet) {
		//for (final net.sf.jsqlparser.schema.Table table : tableSet) {
		for (Iterator<TableInSQL> iterator = tableSet.iterator(); iterator.hasNext();) {
			TableInSQL table = iterator.next();
			if ((table.getAlias() != null)
					&& table.getAlias().equalsIgnoreCase(AliasString)) {
				return table.getName();
			}
		}
		return null;//Can't find one
	}
	
	private List<String> getRecords(final Object[] name){
		List<String> list = null;
		try{
			list = dbConnection.getRecords(tableName, tableIndexes, name);
		}catch(Exception e){
			e.printStackTrace();
		}
		return list;
	}

	public final String getSelectAndIntoClauses(final SelectQueryReader selectQueryReader, String selectQuery) throws Exception{
		
		final Set<ColumnInSQL> selectItems = selectQueryReader.getFirstSelectItemsSet(selectQuery);
		
		List<String> records = null;
		final Map<String,String> columnMap = new HashMap<String,String>(selectItems.size());
		
		for (Iterator<ColumnInSQL> iterator = selectItems.iterator(); iterator.hasNext();) {
			ColumnInSQL selectItem =  iterator.next();
			//String aliasString = "";
			String fullTableName = selectItem.getTableName();
			//recordAdded = false;
			//String tableShortName = selectItem.getTableShortName();
			String displayValue = selectItem.getColumnAlias().equals("") ? selectItem.getTableShortName() :selectItem.getColumnAlias();
			
			if(isAllTableColumns(selectItem)){
				//aliasString = tableShortName;
				//fullTableName = 
				if(!fullTableName.equals("")){
					/*//recordAdded =
						insertFormattedRecordsIntoList(
								aliasString,
						 	    intoRecordsList, 
						 	    this.getRecords(new Object[]{fullTableName})
					            );*/
						records = this.getRecords(new Object[]{fullTableName});
						for (Iterator<String> iterator2 = records.iterator(); iterator2
								.hasNext();) {
							String record = iterator2.next();
							String[] values = record.split(kColon);
							String oneIntoColumn = values[1]+ kColumnAlaisOrTableAlaisFollows +
												displayValue+ kDomainFollows + values[2];
							columnMap.put(selectItem.getSQL() , oneIntoColumn);
						}
						
				}
			}else if(isTableColumn(selectItem)){
				//aliasString = tableShortName;
				//fullTableName = selectItem.getTableName();
				if(!fullTableName.equals("")){
					//recordAdded = 
						/*insertFormattedRecordsIntoList(
								aliasString,
							intoRecordsList,
							this.getRecords(new Object[]{
									fullTableName,
									selectItem.getColumnName()
							}));*/
						records = this.getRecords(new Object[]{
								fullTableName,
								selectItem.getColumnName()
						});
						//should be singleton list
						try{
							String[] values = records.get(0).split(kColon);
							
							String oneIntoColumn = values[1]+ kColumnAlaisOrTableAlaisFollows +
												displayValue+ kDomainFollows + values[2];
							columnMap.put(selectItem.getSQL(), oneIntoColumn);
						}catch(IndexOutOfBoundsException e){
							throw new TableValueNotFoundException("Missing "+"TABLE:<"+fullTableName+"> or COLUMN:<"+selectItem.getColumnName()+">");
						}

						
				}
			}else if(selectItem.isFunctionColumn()){
				String intoVariableStr = "";
				String domainDef = "";
				String columnName = selectItem.getColumnName();
				if(columnName.equals("")){
					if(selectItem.getFunctionName().equalsIgnoreCase("date")){
						// DATE('9999-12-31')
						intoVariableStr = selectItem.getFunctionName().toLowerCase()+ kColumnAlaisOrTableAlaisFollows +
											selectItem.getColumnAlias()+ kDomainFollows + "CURAM_DATE";
						columnMap.put(selectItem.getSQL(), intoVariableStr);
					}else{
						// max(), max(*),  count(), count(*), the following belongs to a different category: max(1),max(claimcycleid)...
						intoVariableStr = selectItem.getFunctionName().toLowerCase()+ kColumnAlaisOrTableAlaisFollows +
										selectItem.getColumnAlias()+ kDomainFollows + "SVR_INT64";
						columnMap.put(selectItem.getSQL(), intoVariableStr);
					}
				}else{
					if(selectItem.getFunctionName().equalsIgnoreCase("date")){
						domainDef = "CURAM_DATE";
						intoVariableStr = columnName + kColumnAlaisOrTableAlaisFollows + displayValue + kDomainFollows + domainDef;
						columnMap.put(selectItem.getSQL(), intoVariableStr);
					}else if(selectItem.getFunctionName().equalsIgnoreCase("count")){
						domainDef = "COUNT";
						intoVariableStr = columnName + kColumnAlaisOrTableAlaisFollows + displayValue + kDomainFollows + domainDef;
						columnMap.put(selectItem.getSQL(), intoVariableStr);
					}else{
						records = this.getRecords(new Object[]{
							fullTableName,
							columnName
						});
						//should be singleton list
						String[] values = records.get(0).split(kColon);
						domainDef = values[2];
						String oneIntoColumn = values[1]+ kColumnAlaisOrTableAlaisFollows +
												displayValue+ kDomainFollows + domainDef;
						columnMap.put(selectItem.getSQL(), oneIntoColumn);
					}
				}
				
			}else{
				//intoRecordsList.add(selectItem.getColumnName().toLowerCase() + "@NEED_HUMAN_INPUT");
				columnMap.put(selectItem.getSQL(), displayValue + "@NEED_HUMAN_INPUT");
			}
		}
		
		final String tab = "\n,	";
		
		String selectClause = "SELECT";
		Set<String> selectList = columnMap.keySet();
		
		for (Iterator<String> iterator = selectList.iterator(); iterator.hasNext();) {
			selectClause += tab+ iterator.next()  ;
		}
		selectClause = selectClause.replaceFirst(",", "");
		String intoClause = "\nINTO";
		for (Iterator<String> iterator = selectList.iterator(); iterator.hasNext();) {
			String columnInSQL = iterator.next();
			intoClause += tab + kColon + columnMap.get(columnInSQL);
		}
		intoClause = intoClause.replaceFirst(",", "");
		return selectClause+ intoClause;
		
	}

	private String getValidatedSelectStatementString(String selectQuery, final SelectQueryReader selectQueryReader) throws Exception {
		//selectQuery = selectQuery.replaceAll(":(\\w)+", "'INPUT'");
		//Statement statement = new CCJSqlParserManager().parse(new StringReader(selectQuery));
		
		final Set<TableInSQL> tableSet = selectQueryReader.getTableSet(selectQuery);
		//checkIfTableAliasAreUnique(tableSet);
		//TODO validateTableName(tableSet);
		validateTableColumns(); //TODO check ALL columns(from every clause) if exist, and if there is ambiguity(e.g.: claimID is in WCOClaim and ClaimCycle )
		
		return selectQueryReader.getParsedSQL(selectQuery);
	}

	private boolean insertFormattedRecordsIntoList(
			final String tableAlias, final List intoRecords,
			                             final List record) {
		for (Iterator iterator = record.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();

			String[] values = string.split(kColon);
			intoRecords.add(values[1]+ kColumnAlaisOrTableAlaisFollows +
					        tableAlias.toUpperCase()+ kDomainFollows + values[2]);
		}

		return true;
	}

	private boolean isAllTableColumns(ColumnInSQL selectItem) {
		//e.g.: "C.*" or "*"
		return (selectItem.getTableShortName() != null) && !selectItem.getTableName().equals("") 
				&& (selectItem.getColumnName()!= null) && selectItem.getColumnName().equals("*")
				&& (selectItem.getTableName()!=null) && !selectItem.getTableName().equals("") 
				&& (selectItem.isFunctionColumn() == false);
	}
	
	private boolean isTableColumn(ColumnInSQL selectItem) {
		//e.g.: C.claimCycleID
		return (selectItem.getColumnName()!=null) && !selectItem.getColumnName().equals("*") 
				&& (selectItem.getTableName()!=null) && !selectItem.getTableName().equals("") 
				&& (selectItem.getTableShortName() !=null) && !selectItem.getTableShortName().equals("")
				&& (selectItem.isFunctionColumn() == false);
	}
	
	
	private void validateTableColumns() {
		//CASE: Select claimID from claimcycle c inner join wcoclaim w;
		
	}



	/*public String getFormatedSQL(final SelectQueryReader selectQueryReader , String selectQuery) throws Exception {
		return selectQueryReader.getFormatedSQL(selectQuery);
	}	*/
	
}
