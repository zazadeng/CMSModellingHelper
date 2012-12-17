package com.wcb.cms.modelmaker.brain.formatter;

import java.util.Set;

/**
 * 
 * This class defines methods needed to form or query a relational database table.
 *
 */
public interface EntityDtlsDBTable{
	
	/**
     * @return a set of Strings representing the name of the index
     */
    Set<String> getCreateIndexQueries();

	/**
     * @return a string representing the sql for creating a table
     */
    String getCreateTableQuery();

    /**
     * @return a set of Strings representing the name of the index
     */
    Set<String> getIndexNames();

    /**
     * @return a set of field names
     */
    //Set getFieldNames();
	
    /**
	 * @return the string for the table name 
	 */
	String getTableName();
	

}