package com.wcb.cms.modelmaker.brain.formatter.db;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;

/**
 * This class is responsible for all the data retrieval from table EntityDtls.
 */
public class EntityDtlsReader {
    public static boolean isNullOrEmpty(String s) {
    	 return ((s == null) || (s.trim().length() == 0));
    }
    /**
      * this reflects entity_Name field in table EntityDtls.
      */
    private final String entityName;
    /**
     * this reflects dtls_Name field in table EntityDtls.
     */
    private final String dtlsName;
    /**
     * this reflects domain_def field in table EntityDtls.
     */
    private final String domainDef;
     /**
     * this reflects entity_search_name field in table EntityDtls.
     */
     private final String entitySearchName;
     /**
      * this reflects dtls_search_name field in table EntityDtls.
      */
     private final String dtlsSearchName;

     /**
      * this reflects the number of fields defines in table EntityDtls.
      */
     private final int numOffields;

	/**
      * This is the default constructor.
      * @param eName entity_name field
      * @param dName dtls_name field
      * @param doDef domain_def field
      * @param eSearchName entity_name_search field
      * @param dSearchName dtls_name_search field
      * @param fieldNum number of fields
      */
     public EntityDtlsReader(//ENTITYDTLS_FIELDS id,
                         final String eName,
                         final String dName,
                         final String doDef,
                         final String eSearchName,
                         final String dSearchName,
                                   final int fieldNum) {
          //this.id = id;
          this.entityName = eName;
          this.dtlsName = dName;
          this.domainDef = doDef;
          this.entitySearchName = eSearchName;
          this.dtlsSearchName = dSearchName;
          this.numOffields = fieldNum;
     }

     private void collect(final List<Map<String, String>> list, final File fileOrDir)  {
		fileOrDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(final File pathname) {
				if (pathname.isDirectory()) {
					collect(list, pathname);
				}
				if (pathname.getName().endsWith("Dtls.java")) {

					list.addAll(parse(pathname));

				}
				return false;
			}
		});
	}

	

	/**
      * Parses content from DTLS java source
      * 
      * @param dirToGetRecords
      *                          the directory to get the records
      * @return List
      *
      */
     public final List<Map<String, String>> obtainRecordsFrom(final String dirToGetRecords) {
        final List<Map<String, String>> records
                         = new ArrayList<Map<String, String> >();
        collect(records, new File(dirToGetRecords));
        return records;
     }

	private List<Map<String, String>> parse(final File file){
		final String dirSperator = File.separator;
		List<Map<String, String>> records = new ArrayList<Map<String, String>>();
		String filePath = file.getPath();
		/*
		 * set entityName note: table name is getting it from the file's name
		 */
		String tableName = filePath.substring(filePath.lastIndexOf(dirSperator) + 1,
				filePath.indexOf("Dtls.java"));
		CharStream c;
		try {
			c = new ANTLRFileStream(filePath);
		} catch (IOException e) {
			System.err.println("ANTLRFileStream can NOT parse this file! Reason:"+ e.getMessage());
			return records;//empty one
		}
		Lexer lexer = new JavaLexer(c);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		TokenSource tokenSource = tokens.getTokenSource();
		Token tk = tokenSource.nextToken();
		do {
			int indexForTable = tk.getText().indexOf(
					"is the generated Standard Details struct");
			int indexForDomain = tk.getText().indexOf("->");
			int endIndex = 0;
			int startIndex = 0;
			if (tk.getType() == JavaLexer.COMMENT) {
				if (indexForTable > 0) {
					// System.out.println(tk.getText());
					endIndex = tk.getText().indexOf(".");
					tableName = tk.getText().substring(0, endIndex);
					startIndex = tableName.lastIndexOf(" ");
					/*
					 * set entityName if it shows up in the content of the file.
					 */
					tableName = tableName.substring(startIndex).trim();
					// System.out.println("table name: "+tableName);
				}
				if (indexForDomain > 0) {
					endIndex = indexForDomain;
					String domainDefName = tk.getText().substring(0, endIndex).trim();
					startIndex = domainDefName.lastIndexOf(" ");
					/*
					 * set domainDefName
					 */
					domainDefName = domainDefName.substring(startIndex).trim();
					// find the identifier belongs to this domain def
					String attributeName = "";
					while (true) {
						if (tk.getType() == JavaLexer.EQ) {
							break;
						}
						/*
						 * set dtlsName
						 */
						attributeName = tk.getText();
						tk = tokenSource.nextToken();
					}
					Map<String, String> oneRecord = new Hashtable<String, String>(numOffields);
					if (isNullOrEmpty(tableName)
							|| isNullOrEmpty(domainDefName)
							|| isNullOrEmpty(attributeName)) {
						System.err.println("File: " + filePath + " has invalid format...");
						return records;//empty one
					}
					/************************************************
					 * set records **********************************
					 ***********************************************/
					oneRecord.put(entityName, tableName);
					oneRecord.put(domainDef, domainDefName);
					oneRecord.put(dtlsName, attributeName);
					oneRecord.put(entitySearchName, tableName.toUpperCase());
					oneRecord.put(dtlsSearchName, attributeName.toUpperCase());
					records.add(oneRecord);
				}
			}
			tk = tokenSource.nextToken();
		} while (tk != Token.EOF_TOKEN);
		
		return records;
	}
}
