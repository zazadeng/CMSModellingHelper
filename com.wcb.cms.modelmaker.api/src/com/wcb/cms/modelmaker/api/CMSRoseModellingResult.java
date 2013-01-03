package com.wcb.cms.modelmaker.api;

import java.util.Map;
/**
 * A communication helper
 */
public interface CMSRoseModellingResult {

	public String getCuramNonStandardSelectQuery();

	public Map<String, String> getInputStruct();

	public Map<String, String> getOutputStruct();

}