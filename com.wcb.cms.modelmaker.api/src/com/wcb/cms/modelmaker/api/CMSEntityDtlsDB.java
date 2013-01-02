package com.wcb.cms.modelmaker.api;

import java.util.List;


public interface CMSEntityDtlsDB {


	/**
	 * Sets domain definition in CMSEntityEntry.
	 * @param list
	 * @throws Exception
	 * 			CAN NOT continue, because it will give us a WRONG answer.
	 */
	void addAttributeAndDomainDefinition(List<CMSEntityEntry> list) throws Exception;

	void connect(String uri);
	/**
	 * Sets future database result in CMSEntityEntry.
	 * @param sqlElements
	 */
	void findInDB(List<CMSEntityEntry> sqlElements);

	void close();

}
