package com.wcb.cms.modelmaker.api;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

public class CMSEntityEntry {
	private String sqlElement;
	private String column;
	private String table;
	private List<String> potentialTableList;
	private String domainDefinition;
	private String columnAlias;
	private Future<String> futureDBValue;

	public CMSEntityEntry(){
		sqlElement = "";
		column = "";
		table = "";
		potentialTableList = Collections.emptyList();
		domainDefinition = "";
		columnAlias = "";
		futureDBValue = null;
	}
	public String getColumn() {
		return column;
	}
	public String getColumnAlias() {
		return columnAlias;
	}
	public String getDomainDefinition() {
		return domainDefinition;
	}
	public Future<String> getFutureDBValue(){
		return this.futureDBValue;
	}
	public List<String> getPotentialTableList() {
		return potentialTableList;
	}
	public String getSqlElement() {
		return sqlElement;
	}
	public String getTable() {
		return table;
	}
	public boolean isFunction() {
		return this.table.matches("FUNCTION:\\w+");
	}
	public CMSEntityEntry setColumn(String column) {
		if(column.equals("*")){
			this.column = column;
		}else{
			this.column = column.replaceAll("\\(.+\\)", "");
			this.columnAlias = column.replaceFirst(this.column, "").replaceAll("[\\(\\)]", "");
		}
		return this;
	}
	public CMSEntityEntry setDomainDefinition(String domainDefinition) {
		this.domainDefinition = domainDefinition;
		return this;
	}
	public void setFutureDBValue(Future<String> future) {
		this.futureDBValue = future;
	}
	public CMSEntityEntry setPotentialTableList(List<String> potentialTableList) {
		this.potentialTableList = potentialTableList;
		if(potentialTableList.size() == 1){
			this.table = potentialTableList.get(0);
		}
		return this;
	}
	public CMSEntityEntry setSqlElement(String sqlElement) {
		this.sqlElement = sqlElement;
		return this;
	}
	public CMSEntityEntry setTable(String table) {
		this.table = table;
		return this;
	}


}
