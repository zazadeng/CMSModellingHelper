package com.wcb.cms.modelmaker.persistent.sqlite;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.schema.SqlJetConflictAction;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTransaction;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import com.wcb.cms.modelmaker.api.CMSEntityDtlsDB;


/**
 * This is the helper class to use SqlJet to connect
 * to Sqlite database.
 */
public class SqliteDbConnector implements CMSEntityDtlsDB{
	
	private SqlJetDb db;
	
	public SqliteDbConnector(){
		
	}
    
	@Override
	public void close() throws SqlJetException {
		this.db.close();
	}

	@Override
	public CMSEntityDtlsDB createDatabase(final String dbName)
	           throws SqlJetException {
		  SqliteDbConnector connector = new SqliteDbConnector();
	      final java.io.File dbFile = new java.io.File(dbName);
	      dbFile.delete();
	      final SqlJetDb db = new SqlJetDb(dbFile, true);
	      //final SqlJetDb db = new SqlJetDb(SqlJetDb.IN_MEMORY, true);
	      db.open();
	      db.getOptions().setAutovacuum(true);
	      db.runTransaction(new ISqlJetTransaction() {
	           @Override
			public Object run(final SqlJetDb theDB)
	                               throws SqlJetException {
	        	 theDB.getOptions().setUserVersion(1);
	        	 return null;
	           }
	      }, SqlJetTransactionMode.WRITE);
	      
	      //java 1.4 is implemented below:
	      /*db.beginTransaction(SqlJetTransactionMode.WRITE);
	      try{
	    	  db.getOptions().setUserVersion(1);
	      }catch(SqlJetException e){
	    	  db.rollback();
	    	  throw new SqlJetException(e);
	      }finally{
	    	  db.commit();
	      }*/
	      
	      connector.db = db;
	      return connector;
	}

     @Override
	public CMSEntityDtlsDB createInMemoryDatabase()
               throws SqlJetException {
		return createDatabase(SqlJetDb.IN_MEMORY.getPath());
    }

     @Override
	public CMSEntityDtlsDB getDBForReadOnly(String dbPath) throws SqlJetException {
    	SqliteDbConnector connector = new SqliteDbConnector();
    	SqlJetDb aDB = SqlJetDb.open(new File(dbPath), false);
    	connector.db = aDB;
    	return connector;
	}

    @Override
	public List<String> getRecords(
                                   String tableName,Set<String> tableIndexes,
                                   final Object[] values)
                                   throws SqlJetException {
          final List<String> strList = new ArrayList<String>();
          db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
          try{

              final Object[] lookupValues
                        = new Object[values.length];
              for (int i = 0; i < values.length; i++) {
                   lookupValues[i] =
                        values[i].toString()
                                  .toUpperCase();
              }
             /* final Set<String> indexNames
                             = table.getIndexNames();*/
              for (Iterator<String> iterator = tableIndexes.iterator(); iterator
						.hasNext();) {
				String indexName = iterator.next();
				final ISqlJetCursor cursor
                                  = db.getTable(
                                		  tableName)
                              .lookup(indexName,
                                  lookupValues);
                   if (!cursor.eof()) {
                        do {
                             strList.add(
                              cursor
                               .getString(0) + ":"
                              + cursor
                               .getString(1) + ":"
                              + cursor.getString(2));
                        } while (cursor.next());
                   }
                   cursor.close();
              }
              
          }catch(SqlJetException e){
        	  //no rollback is needed...
        	  throw new SqlJetException(e);
          }finally{
        	  db.commit();
          }

          return strList;
     }
	
     /**
      *
      * @param sqliteDb
      *                database connection
      * @param tableName
      *                the name of the table to be inserted
      * @param records
      *                the records for inserting to the table
      * @throws Exception Exception for SqlJet
      */
     @Override
	public void insertRecords(
                                   final String tableName,
               final List<?> records)
                                   throws Exception {
        for (Iterator<?> iterator = records.iterator(); iterator.hasNext();) {
			final Map record = (Map) iterator.next();
			System.out.println(record.toString());
			
			//db.pragma("PRAGMA locking_mode = EXCLUSIVE");
			
			db.beginTransaction(SqlJetTransactionMode.EXCLUSIVE);
			db.setCacheSize(4000);
			try{
			 db.getTable(tableName)
             .insertByFieldNamesOr(
                  SqlJetConflictAction.REPLACE,
                  record);
			}catch(Exception e){
	        	  db.rollback();
	        	  throw new Exception(e);
	         }finally{
	        	  db.commit();
	         }
				
         }
     
     }
	
	/**
      *
      * @param sqliteDb
      *                database connection
      * @param createTableQuery
      *                query for creating a table
      * @param indexQueries
      *                query for creating index(es)
      * @throws Exception Exception for SqlJet
      */
     @Override
	public void setupTableAndIndexes(
               final String createTableQuery,
               final java.util.Set<String> indexQueries)
               throws Exception {
          db.runTransaction(new ISqlJetTransaction() {
               @Override
			public Object run(final SqlJetDb theDb)
                                   throws SqlJetException {
                    theDb.createTable(createTableQuery);
                    for (Iterator<?> iterator = indexQueries.iterator(); iterator
							.hasNext();) {
						String query = (String) iterator.next();
						
//					}
//                    for (final String query : indexQueries) {
                         theDb.createIndex(query);
                    }
                    return null;
               }
          }, SqlJetTransactionMode.WRITE);
          //java 1.4 implementation is below:
         /* db.beginTransaction(SqlJetTransactionMode.WRITE);
          try{
              db.createTable(createTableQuery);
              for (Iterator iterator = indexQueries.iterator(); iterator
						.hasNext();) {
				String query = (String) iterator.next();
                db.createIndex(query);
              }
          }catch(Exception e){
        	  db.rollback();
        	  throw new Exception(e);
          }finally{
        	  db.commit();
          }*/

     }
	
}
