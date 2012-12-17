package com.wcb.cms.modelmaker.api;

import java.util.List;
import java.util.Set;

public interface SelectQueryReader {

	public Set<TableInSQL> getTableSet(String selectQuery) throws Exception;

	public String getTopLevelFromItemString(final String statement) throws Exception;

	public Set getTableColumnsSet(String sql);
	
	public List<String> getEqualToStringValueExprssionList(String sql) throws Exception;

	public List<String> getInStringValueExprssionList(String sql) throws Exception;

	public Set<ColumnInSQL> getFirstSelectItemsSet(String sql) throws Exception;

	public String getParsedSQL(String selectQuery) throws Exception;

	public List<String> getBetweenStringValueExprssionList(String selectSql) throws Exception;

}