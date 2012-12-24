package com.wcb.cms.modelmaker.brain.core;

import java.util.Hashtable;
import java.util.Map;

public class RoseModelHelper {

	public static String constructDynamicSQL(String metaSql) {
		//exception :sec2331AsmtRsltID_WCAL@SECTION_23_3_1_ASSESSMENT_RESULT_ID
		metaSql = metaSql.replaceAll("@([A-Z|0-9]+)(_([A-Z|0-9]+))+", "");
		//metaSql = metaSql.replaceAll("@.*", "");
		return metaSql;
	}

	public static Map constructInputStruct(String metaSql) {
		int INTO_index = metaSql.indexOf("INTO");
		metaSql = metaSql.substring(INTO_index);
		int FROM_index = metaSql.indexOf("FROM");
		metaSql = metaSql.substring(FROM_index);

		Map map = formStructDetailsMap(metaSql);

		return map;
	}

	public static Map constructOutputStruct(String metaSql) {
		int INTO_index = metaSql.indexOf("INTO");
		metaSql = metaSql.substring(INTO_index);
		int FROM_index = metaSql.indexOf("FROM");
		metaSql = metaSql.substring(0,FROM_index);

		Map map = formStructDetailsMap(metaSql);

		return map;
	}

	/*public static String constructSelectQueryForRoseModel(String selectQuery,
			CMSEntityDtlsDB dbConnection) throws Exception {
		CMSEntityRecordFinder entityFinder =  new CMSEntityRecordFinder(selectQuery);
		String selectAndIntoStatement = entityFinder.getSelectAndIntoClauses(selectQueryReader, selectQuery);
		String constantReplacedQuery = entityFinder.getConstantsPlaceHolders(selectQueryReader, selectQuery);

		final String cariageReturn = " \n";
		//final String theQuery = entityFinder.getValidatedSelectStatementString(selectQuery);
		final String theQuery = selectQueryReader.getParsedSQL(selectQuery);//entityFinder.getFormatedSQL(selectQuery);

		final String firstFromItem = selectQueryReader.getTopLevelFromItemString(theQuery);

		final int fromIndex = theQuery
				.substring(0, theQuery.indexOf(firstFromItem)).toLowerCase()
				.lastIndexOf("from");
		//String intoQuery = theQuery.substring(0, fromIndex) + cariageReturn + intoStatement + cariageReturn;
		String intoQuery = selectAndIntoStatement + cariageReturn;
		//+ theQuery.substring(fromIndex);
		//return selectQueryReader.getFormatedSQL(intoQuery + constantReplacedQuery.substring(fromIndex));
		return intoQuery + constantReplacedQuery.substring(fromIndex);

	}*/

	private static Map formStructDetailsMap(String metaSql) {
		Map map = new Hashtable();

		String[] tokens = metaSql.split("(\\s)*=(\\s)*|(\\s)*,(\\s)*|(\\s)+|\\(|\\)");
		for(int i = 0 ; i < tokens.length; i++){
			if(tokens[i].startsWith(":")){
				String[] splitValues = tokens[i].split("@");
				try{
					map.put(splitValues[0].substring(1), splitValues[1]);
				}catch(ArrayIndexOutOfBoundsException e){
					System.out.println(splitValues);
				}
				//placeHolderTable.put(placeHolderStr, string);
			}
		}
		return map;
	}

}
