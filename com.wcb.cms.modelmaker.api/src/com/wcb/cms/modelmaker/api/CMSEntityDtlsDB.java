package com.wcb.cms.modelmaker.api;

import java.util.List;
import java.util.Map;


public interface CMSEntityDtlsDB {

	List<Map<String, String>> addDomainDefinition(
			List<Map<String, String>> intoClauseList);

	String addDomainDefinitionForIntoClause(List<String> keyList);

	void connect(String uri);


}
