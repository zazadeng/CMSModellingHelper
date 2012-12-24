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
	 * 
	 * Deal with constant and variables after the top level FROM; although it is totally legitimate to place constants or variables before, we will see if there are real cases...
	 * @param selectQuery
	 * @return
	 */
	public List<Map<String, String>> retrieveConstantAndVariable(
			String selectQuery);

	/**
	 * Deal with columns within the top level SELECT clause.
	 * KEY in the map is a column name or a
	 * VALUE in the map is a list of table names associated with the column(key)
	 * @param selectQuery
	 * @return
	 * @throws Exception
	 */
	public Map<String, List<String>> retrieveIntoClause(String selectQuery) throws Exception;

}