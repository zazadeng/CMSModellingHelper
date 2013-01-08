package com.wcb.cms.modelmaker.api;



public interface AppInterface {

	public void close();

	public CMSRoseModellingResult transformSelectQuery(final String selectQuery,
			final String dbPath) throws Exception;

}