package com.wcb.cms.modelmaker.brain.core;

import java.util.List;

import com.wcb.cms.modelmaker.api.AppInterface;
import com.wcb.cms.modelmaker.api.CMSEntityDtlsDB;
import com.wcb.cms.modelmaker.api.CMSEntityEntry;
import com.wcb.cms.modelmaker.api.CMSRoseModellingResult;
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

	// Injected via blueprint
	public void setCmsEntityDtlsDB(CMSEntityDtlsDB cmsEntityDtlsDB) {
		this.cmsEntityDtlsDB = cmsEntityDtlsDB;
	}

	// Injected via blueprint
	public void setSelectQueryReader(SelectQueryReader selectQueryReader) {
		this.selectQueryReader = selectQueryReader;
	}

	@Override
	public final CMSRoseModellingResult transformSelectQuery(final String selectQuery,
			final String uri)
					throws Exception {
		final List<CMSEntityEntry> intoClauseList =
				selectQueryReader.retrieveIntoClause(selectQuery);
		cmsEntityDtlsDB.connect(uri);
		cmsEntityDtlsDB.findInDB(intoClauseList);//Async

		final List<CMSEntityEntry> constAndVarialbeList =
				selectQueryReader.retrieveConstantAndVariable(selectQuery);
		cmsEntityDtlsDB.findInDB(constAndVarialbeList);//Async

		cmsEntityDtlsDB.addAttributeAndDomainDefinition(intoClauseList);
		cmsEntityDtlsDB.addAttributeAndDomainDefinition(constAndVarialbeList);

		//cmsEntityDtlsDB.close();//Why close? Will the connection still preserved while the application is down?

		final RoseModellingResultImpl result = new RoseModellingResultImpl(
				selectQuery,
				intoClauseList,
				constAndVarialbeList);
		return result;

	}



}
