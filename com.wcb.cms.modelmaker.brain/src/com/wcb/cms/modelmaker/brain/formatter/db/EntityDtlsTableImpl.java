package com.wcb.cms.modelmaker.brain.formatter.db;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wcb.cms.modelmaker.brain.formatter.EntityDtlsDBTable;
import com.wcb.cms.modelmaker.brain.formatter.JavaDtlsFileReader;


/**
 * This is an entity representing a table in a relational database;
 * it defines what we need to create for the table in the database and
 * what we have to do to insert a record of this table to the database as well.
 *
 */
public class EntityDtlsTableImpl implements EntityDtlsDBTable, JavaDtlsFileReader {
    public static class ENTITYDTLS_FIELDS {
       final public static String ENTITY_NAME = "ENTITY_NAME";
       final public static String DTLS_NAME = "DTLS_NAME";// DTLS_NAME,
       final public static String DOMAIN_DEF = "DOMAIN_DEF";//DOMAIN_DEF,
       final public static String ENTITY_SEARCH_NAME = "ENTITY_SEARCH_NAME";//ENTITY_SEARCH_NAME,
       final public static String DTLS_SEARCH_NAME = "DTLS_SEARCH_NAME";//DTLS_SEARCH_NAME;
     }
    
    private final String kTableName = "entitydtls";
     
    private final String createTableQueryForSqlite = "CREATE TABLE " + kTableName
               + " ("
                //+ ENTITYDTLS_FIELDS.ID + " INTEGER NOT NULL PRIMARY KEY , "
               + ENTITYDTLS_FIELDS.ENTITY_NAME + " TEXT NOT NULL , "
               + ENTITYDTLS_FIELDS.DTLS_NAME + " TEXT NOT NULL , "
               + ENTITYDTLS_FIELDS.DOMAIN_DEF + " TEXT NOT NULL , "
               + ENTITYDTLS_FIELDS.ENTITY_SEARCH_NAME + " TEXT NOT NULL , "
               + ENTITYDTLS_FIELDS.DTLS_SEARCH_NAME + " TEXT NOT NULL "
               + ")";

     private final String kEntityDtlsSearchIndex
                                 = "entity_name_dtls_search_index";
     
     private final String createEntityNameDtlsNameIndexQueryForSqlite
                            = "CREATE UNIQUE INDEX "
                                     + kEntityDtlsSearchIndex + " ON "
                                     + kTableName
                                     + "("
                                     + ENTITYDTLS_FIELDS.ENTITY_SEARCH_NAME
                                     + ","
                                     + ENTITYDTLS_FIELDS.DTLS_SEARCH_NAME + ")";

     private final int numOffields = ENTITYDTLS_FIELDS.class.getFields().length;

     @Override
	public final Set<String> getCreateIndexQueries() {
          return Collections.singleton(createEntityNameDtlsNameIndexQueryForSqlite);
     }
     
     @Override
	public final String getCreateTableQuery() {
          return createTableQueryForSqlite;
     }
     
     /**
      * @param dirToGetRecords the directory to get records
      * @return a list of "field-value" pairs
      * 			e.g.: {ENTITY_NAME=DisplayUserSchedule, DTLS_SEARCH_NAME=CASEID, ENTITY_SEARCH_NAME=DISPLAYUSERSCHEDULE, DTLS_NAME=caseID, DOMAIN_DEF=CASE_ID}
      * @throws IOException exception
      * @throws InvalidFileFormatException exception
      */
     @Override
	public final List<Map<String, String>> getFormatedRecords(
    		 							final String dirToGetRecords)
    		 			 {
          
          return new com.wcb.cms.modelmaker.brain.formatter.db.EntityDtlsReader(
                    ENTITYDTLS_FIELDS.ENTITY_NAME, 
                    ENTITYDTLS_FIELDS.DTLS_NAME,
                    ENTITYDTLS_FIELDS.DOMAIN_DEF, 
                    ENTITYDTLS_FIELDS.ENTITY_SEARCH_NAME,
                    ENTITYDTLS_FIELDS.DTLS_SEARCH_NAME, numOffields)
                                   .obtainRecordsFrom(dirToGetRecords);
     }
     
     @Override
	public final Set<String> getIndexNames() {
          java.util.Set<String> indexes = new java.util.HashSet<String>();
          indexes.add(kEntityDtlsSearchIndex);
          return indexes;
     }
     
     @Override
	public final String getTableName() {
          return kTableName;
     }
     

}
