package com.wcb.cms.modelmaker.api;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SelectQueryReader {

	public String composeCuramNonStandardSelectQuery(String selectQuery,
			List<Map<String, String>> intoStatementMetaData,
			List<Map<String, String>> constAndVarialbeMetaDataList);

	public String composeSelectQuery(
			List<Map<String, String>> intoClauseList,
			List<Map<String, String>> constAndVarialbeList);

	public List<String> getBetweenStringValueExprssionList(String selectSql) throws Exception;

	public List<String> getEqualToStringValueExprssionList(String sql) throws Exception;

	public Set<ColumnInSQL> getFirstSelectItemsSet(String sql) throws Exception;

	public List<String> getInStringValueExprssionList(String sql) throws Exception;

	public String getParsedSQL(String selectQuery) throws Exception;

	public String getSelectAndIntoClauses(String selectQuery) throws Exception;

	public Set getTableColumnsSet(String sql);

	public Set<TableInSQL> getTableSet(String selectQuery) throws Exception;

	public String getTopLevelFromItemString(final String statement) throws Exception;

	/**
	 * Finds everything Constant and Variable element of a SQL query
	 * and populates information(table, column, alias ...) into CMSEntityEntry.
	 * 
	 * @param selectQuery
	 * 			a string representation of a SQL query
	 * @return a list of information needed to extract Domain definitions
	 * 
	 * @throws Exception
	 * 			most likely SQLParserInternalException or SQLParserException
	 */
	public List<CMSEntityEntry> retrieveConstantAndVariable(
			String selectQuery) throws Exception;

	/**
	 * Loops through the First SELECT clause of a SQL query,
	 * and populates information(table, column, alias ...) into CMSEntityEntry.
	 * 
	 * @param selectQuery
	 * 			a string representation of a SQL query
	 * @return a list of information needed to extract Domain definitions
	 * @throws Exception
	 * 				most likely SQLParserInternalException or SQLParserException
	 */
	public List<CMSEntityEntry> retrieveIntoClause(String selectQuery) throws Exception;

}