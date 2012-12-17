package com.wcb.cms.modelmaker.brain.formatter;

import java.io.IOException;
import java.util.List;

public interface JavaDtlsFileReader {
	  /**
     * @param dirToGetRecords the directory to get records
     * @return a list of "field-value" pairs(map), or a list of CMSEntityRecord
     * @throws IOException exception
     */
	public abstract List<?> getFormatedRecords(
			final String dirToGetRecords) throws IOException;

}