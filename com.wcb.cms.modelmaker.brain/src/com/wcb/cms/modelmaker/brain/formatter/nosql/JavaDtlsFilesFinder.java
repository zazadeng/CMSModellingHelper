package com.wcb.cms.modelmaker.brain.formatter.nosql;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

import com.wcb.cms.modelmaker.brain.formatter.JavaDtlsFileReader;

/**
 * This class finds generated Java Dtls files 
 * and populate data for NoSQL database.
 * 
 * @author Zaza
 * 
 *
 */
public class JavaDtlsFilesFinder implements JavaDtlsFileReader {

	private EntityDtlsJavaFileParser entityDtlsJavaFileParser;
	
	private void findJavaDtlsFile(final File root) {
		root.listFiles(new FileFilter() {
			@Override
			public boolean accept(final File file) {
				if (file.isDirectory()) {
					findJavaDtlsFile(file);
					return false;
				}
				if (file.getName().endsWith("Dtls.java")) {
					entityDtlsJavaFileParser.parse(file);
					//return false; why bother ...
				}
				return false;
			}
		});
	}
	

	@Override
	public final List<CMSEntityRecord> getFormatedRecords(
            final String dirToGetRecords) {
		entityDtlsJavaFileParser = new EntityDtlsJavaFileParser();
		findJavaDtlsFile(new File(dirToGetRecords));
		return entityDtlsJavaFileParser.getResult();
	}

}

