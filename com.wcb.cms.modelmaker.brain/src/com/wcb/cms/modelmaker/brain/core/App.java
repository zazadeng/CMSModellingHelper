package com.wcb.cms.modelmaker.brain.core;

import java.util.List;

import com.wcb.cms.modelmaker.api.AppInterface;
import com.wcb.cms.modelmaker.api.CMSEntityDtlsDB;
import com.wcb.cms.modelmaker.api.CMSEntityEntry;
import com.wcb.cms.modelmaker.api.CMSRoseModellingResult;
import com.wcb.cms.modelmaker.api.SelectQueryReader;

/**
 *
 * The hub to glue all dependent bundle objects.
 * The servlet will only hold ONE instance of
 * this class, and consequently one instance of this class's field(bundle objects),
 * so in order to achieve concurrency, RoseModellingResultImpl is implemented.
 * 
 */
public class App implements AppInterface {
	/**
	 * for data persistent, Redis
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

	@Override
	public void close() {
		cmsEntityDtlsDB.close();
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
	public final CMSRoseModellingResult transformSelectQuery(final String selectQuery,
			final String uri)
					throws Exception {
		/**
		 * memory sharing zone: we will have only ONE instance of THIS initialized in the HTTP servlet
		 */
		List<CMSEntityEntry> intoClauseList;
		List<CMSEntityEntry> constAndVarialbeList;
		synchronized (this) {
			intoClauseList =
					selectQueryReader.retrieveIntoClause(selectQuery);
			constAndVarialbeList =
					selectQueryReader.retrieveConstantAndVariable(selectQuery);
		}
		cmsEntityDtlsDB.connect(uri);
		cmsEntityDtlsDB.findInDB(intoClauseList);//Async
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
