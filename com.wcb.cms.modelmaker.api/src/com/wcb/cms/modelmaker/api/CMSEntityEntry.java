package com.wcb.cms.modelmaker.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

/**
 * A helper class for communication
 */
public final class CMSEntityEntry {
	private static final String UNDERSCORE = "_";
	public static synchronized void resetVariableSuffix() {
		Character = 'z';
	}
	private String sqlElement;
	private String column;
	private String table;
	private List<String> potentialTableList;
	private String domainDefinition;
	private String columnAlias;
	private Future<String> futureDBValue;
	private String attribute;
	private List<String> variableList;

	private static char Character = 'z';
	public CMSEntityEntry(){
		sqlElement = "";
		column = "";
		table = "";
		potentialTableList = Collections.emptyList();
		domainDefinition = "";
		columnAlias = "";
		futureDBValue = null;
		this.variableList =  null;

	}
	public CMSEntityEntry addVariable(String variable) {
		if(this.variableList == null){
			this.variableList = new ArrayList<String>();
		}
		//no need to lock, the order of this variable list
		// is not important ...
		this.variableList.add(variable + UNDERSCORE +nextCharacter());
		return this;
	}
	public String getAttribute() {
		return this.attribute;
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
	public Future<String> getFutureDBReturnValue(){
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
	public String getVariable(String variable) {
		for (String var : variableList) {
			if(var.startsWith(variable+UNDERSCORE)) {
				return var;
			}
		}
		return "";
	}
	public List<String> getVariableList(){
		return this.variableList;
	}
	public boolean isFunction() {
		return this.table.matches("FUNCTION:\\w+");
	}
	private synchronized char nextCharacter() {
		if(Character == 'z'){
			Character ='a';
		}
		return Character++;
	}
	public CMSEntityEntry setAttribute(String attr) {
		this.attribute = attr;
		return this;
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
	public synchronized CMSEntityEntry setFutureDBValue(Future<String> future) {
		futureDBValue = future;
		return this;
	}
	public CMSEntityEntry setPotentialTableList(List<String> potentialTableList) {
		this.potentialTableList = Collections.list(Collections.enumeration(potentialTableList));//clone
		if(this.potentialTableList.size() == 1){
			this.table = this.potentialTableList.get(0);
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
