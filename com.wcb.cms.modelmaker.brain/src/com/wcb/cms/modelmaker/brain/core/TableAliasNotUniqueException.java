package com.wcb.cms.modelmaker.brain.core;

public class TableAliasNotUniqueException extends Exception {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2372519337383015186L;
	private String tableA;
	private String tableB;
	private String alias;

	public TableAliasNotUniqueException(String tableA, String tableB, String alias) {
		super(tableA + " and " + tableB + " have the same alias \""+ alias +"\"");
		this.tableA = tableA;
		this.tableB = tableB;
		this.alias = alias;
	}

	public String getTableA() {
		return tableA;
	}

	public String getTableB() {
		return tableB;
	}

	public String getAlias() {
		return alias;
	}
	
}
