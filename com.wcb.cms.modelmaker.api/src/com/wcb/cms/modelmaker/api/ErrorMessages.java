package com.wcb.cms.modelmaker.api;


public class ErrorMessages {
	/**
	 * Indicates that we could not find a value in a database based on
	 * a given table-column pair.
	 * @param columnInUpperCase
	 * @param tableInUpperCase
	 * @return
	 */
	public static String error1(String columnInUpperCase, String tableInUpperCase){
		return "There is NO return value from the presistence; please check if this column <"
				+ columnInUpperCase + "> is indeed in table <"
				+ tableInUpperCase + ">.";
	}
	/**
	 * Indicates that we could not parse the value which is returned from
	 * a database.
	 * @param data
	 * @return
	 */
	public static String error2(String data){
		return "This returned value \""
				+ data
				+ "\" is not formatted correctly, please check on the persistent entry.";
	}
	/**
	 * Indicates that we can NOT find the service name in the context...
	 * NOTE: check if we have to add "javax.naming" dependency in the manifest file.
	 * @param osgiServiceName
	 * @param reason
	 * @return
	 */
	public static String error3(String osgiServiceName, String reason){
		return "Can't find OSGi Service named: \"" + osgiServiceName+"\"\n"
				+ reason;
	}
}
