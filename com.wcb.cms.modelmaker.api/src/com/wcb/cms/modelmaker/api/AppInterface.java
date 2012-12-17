package com.wcb.cms.modelmaker.api;

import java.util.Map;

public interface AppInterface {

	/**
	 * construct an INTO clause using the table records in the db.
	 * @param selectQuery
	 *                the "select" query
	 * @param dbPath
	 *                the path of the db
	 * @return a string of SELECT query with entity information embedded
	 * @throws Exception 
	 */
	public String addPlaceHoldersForSelectQuery(final String selectQuery,
			final String dbPath) throws Exception;

	/**
	 * create a database.
	 * @param fileName
	 *                db name to be set
	 * @param dirToGetRecords
	 *                directory to get records
	 * @return true if a db is created successfully
	 * @throws Exception 
	 */
	public boolean createDB(final String fileName, final String dirToGetRecords)
			throws Exception;

	public String getDynamicSqlForRose(String metaSql);

	public Map getInputStructForRose(String metaSql);

	public Map getOutputStructForRose(String metaSql);

}