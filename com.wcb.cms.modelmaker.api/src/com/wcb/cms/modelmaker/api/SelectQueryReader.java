package com.wcb.cms.modelmaker.api;

import java.util.List;

public interface SelectQueryReader {

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