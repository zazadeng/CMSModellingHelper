package com.wcb.cms.modelmaker.brain.core;

import java.util.List;
import java.util.Map;

import com.wcb.cms.modelmaker.api.AppInterface;
import com.wcb.cms.modelmaker.api.CMSEntityDtlsDB;
import com.wcb.cms.modelmaker.api.SelectQueryReader;
import com.wcb.cms.modelmaker.brain.formatter.EntityDtlsDBTable;
import com.wcb.cms.modelmaker.brain.formatter.JavaDtlsFileReader;
import com.wcb.cms.modelmaker.brain.formatter.db.EntityDtlsTableImpl;

/**
 *
 * entry point of the application.
 *
 */
public class App implements AppInterface {
     /**
      * SqlJetDb object from SqlJet.
      */
     private CMSEntityDtlsDB cmsEntityDtlsDB; 
     /**
      * the definition of the table
      * to be manipulated on.
      */
    // private EntityDtlsDBTable entityDtlsTable;  

     /**
      * Select Query Reader using DTP plugins
      */
     private SelectQueryReader selectQueryReader;
     
     /**
      * construct an INTO clause using the table records in the db.
      * @param selectQuery
      *                the "select" query
      * @param dbPath
      *                the path of the db
      * @return a string of SELECT query with entity information embedded
      * @throws Exception 
      */
    @Override
	public final String addPlaceHoldersForSelectQuery(final String selectQuery,
                                                           final String dbPath)
                                                        		   throws Exception {
          /*if ((cmsEntityDtlsDB == null) && (dbPath != null)) {
               //sqliteDb = SqlJetDb.open(new File(dbPath), false);
        	  cmsEntityDtlsDB = cmsEntityDtlsDB.getDBForReadOnly(dbPath);
          }*/
    	 cmsEntityDtlsDB = cmsEntityDtlsDB.getDBForReadOnly(dbPath);
          return RoseModelHelper
          			.constructSelectQueryForRoseModel(selectQuery, 
          					cmsEntityDtlsDB, selectQueryReader);
     }

     /**
      * create a database.
      * @param fileName
      *                db name to be set
      * @param dirToGetRecords
      *                directory to get records
      * @return true if a db is created successfully
     * @throws Exception 
      */
     @Override
	public final boolean createDB(
                            final String fileName, final String dirToGetRecords)
               throws Exception {
          /*
           * dbms connection
           */
         //final SqlJetDb db = SqliteDbConnector.createDatabase(fileName);
         //dbc = CMSEntityDtlsDBFactory.newDB(fileName);
    	 cmsEntityDtlsDB = cmsEntityDtlsDB.createDatabase(fileName);
    	 
    	 EntityDtlsDBTable entityDtlsTable = new EntityDtlsTableImpl();
          /*
           * table to insert to the database
           */
         cmsEntityDtlsDB.setupTableAndIndexes(entityDtlsTable.getCreateTableQuery(),
                 entityDtlsTable.getCreateIndexQueries());
                 
          /*
           * get formated data
           */
          final List<?> records = ((JavaDtlsFileReader)entityDtlsTable).getFormatedRecords(
                                                          dirToGetRecords);
          /*
           * insert records into database, heavy I/O ...
           */
          cmsEntityDtlsDB.insertRecords(entityDtlsTable.getTableName(), records);
         
          return true;
     }
     
	@Override
	public String getDynamicSqlForRose(String metaSql) {
		return RoseModelHelper.constructDynamicSQL(metaSql);
	}

     @Override
	public final Map getInputStructForRose(String metaSql) {
		return RoseModelHelper.constructInputStruct(metaSql);
	}

	@Override
	public Map getOutputStructForRose(String metaSql) {
		return RoseModelHelper.constructOutputStruct(metaSql);
	}

	// Injected via blueprint
     public void setCmsEntityDtlsDB(CMSEntityDtlsDB cmsEntityDtlsDB) {
		this.cmsEntityDtlsDB = cmsEntityDtlsDB;
     }

	// Injected via blueprint
     public void setSelectQueryReader(SelectQueryReader selectQueryReader) {
 		this.selectQueryReader = selectQueryReader;
 	 }
	

	
}
