package com.wcb.cms.modelmaker.api;



public interface AppInterface {

	public CMSRoseModellingResult transformSelectQuery(final String selectQuery,
			final String dbPath) throws Exception;

}