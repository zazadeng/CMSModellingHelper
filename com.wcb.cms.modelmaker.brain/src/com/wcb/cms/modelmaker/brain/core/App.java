package com.wcb.cms.modelmaker.brain.core;

import java.util.List;
import java.util.Map;

import com.wcb.cms.modelmaker.api.AppInterface;
import com.wcb.cms.modelmaker.api.CMSEntityDtlsDB;
import com.wcb.cms.modelmaker.api.RoseModellingResult;
import com.wcb.cms.modelmaker.api.SelectQueryReader;

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

	private String composeCuramNonStandardSelectQuery(String parsedSQL,
			List<Map<String, String>> intoStatementMetaDataList,
			List<Map<String, String>> constAndVarialbeMetaDataList) {
		// TODO Auto-generated method stub
		return null;
	}

	private String composeInputStruct(
			List<Map<String, String>> intoStatementMetaDataList) {
		// TODO Auto-generated method stub
		return null;
	}

	private String composeOutputStruct(
			List<Map<String, String>> constAndVarialbeMetaDataList) {
		// TODO Auto-generated method stub
		return null;
	}

	// Injected via blueprint
	public void setCmsEntityDtlsDB(CMSEntityDtlsDB cmsEntityDtlsDB) {
		this.cmsEntityDtlsDB = cmsEntityDtlsDB;
	}

	// Injected via blueprint
	public void setSelectQueryReader(SelectQueryReader selectQueryReader) {
		this.selectQueryReader = selectQueryReader;
	}

	@Override
	public final RoseModellingResult transformSelectQuery(final String selectQuery,
			final String uri)
					throws Exception {
		/*Map<String, Map<String, List<String>>> intoClauseList = selectQueryReader.retrieveIntoClause(selectQuery);
		cmsEntityDtlsDB.connect(uri);
		List<Map<String,String>> intoStatementMetaDataList = cmsEntityDtlsDB.addDomainDefinition(intoClauseList);


		Map<String, Map<String, List<String>>> constAndVarialbeList = selectQueryReader.retrieveConstantAndVariable(selectQuery);
		List<Map<String,String>> constAndVarialbeMetaDataList = cmsEntityDtlsDB.addDomainDefinition(constAndVarialbeList);

		//List<String> results = new ArrayList<>(3);
		RoseModellingResult result = new RoseModellingResult();
		result.composeCuramNonStandardSelectQuery(selectQueryReader.getParsedSQL(selectQuery), intoStatementMetaDataList, constAndVarialbeMetaDataList);
		result.composeInputStruct(intoStatementMetaDataList);
		result.composeOutputStruct(constAndVarialbeMetaDataList);
		return result;*/
		return null;


		/*String selectAndIntoStatement = selectQueryReader.getSelectAndIntoClauses(selectQuery);
		String constantReplacedQuery = selectQueryReader.getConstantsPlaceHolders(selectQuery);
		final String cariageReturn = " \n";
		final String theQuery = selectQueryReader.getParsedSQL(selectQuery);

		final String firstFromItem = selectQueryReader.getTopLevelFromItemString(theQuery);

		final int fromIndex = theQuery
				.substring(0, theQuery.indexOf(firstFromItem)).toLowerCase()
				.lastIndexOf("from");
		String intoQuery = selectAndIntoStatement + cariageReturn;
		return intoQuery + constantReplacedQuery.substring(fromIndex);*/
	}



}
