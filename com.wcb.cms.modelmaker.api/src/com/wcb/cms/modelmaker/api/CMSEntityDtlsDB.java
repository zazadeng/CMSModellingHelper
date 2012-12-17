/**
 * 
 */
package com.wcb.cms.modelmaker.api;

import java.util.List;
import java.util.Set;





/**
 * Every connector(in this case, sqlite) has to implement this interface.
 * 
 */
public interface CMSEntityDtlsDB {

	void close() throws Exception;

	CMSEntityDtlsDB createDatabase(String fileName) throws Exception;

	CMSEntityDtlsDB createInMemoryDatabase()throws Exception;

	CMSEntityDtlsDB getDBForReadOnly(String dbPath)throws Exception;

	List<String> getRecords(
            String tableName, Set<String> tableIndexes,
            final Object[] values)throws Exception;

	void insertRecords(String tableName, List<?> records) throws Exception;

	void setupTableAndIndexes(String createTableQuery,
			Set<String> createIndexQueries) throws Exception;

	

	
}
