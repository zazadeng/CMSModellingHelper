package com.wcb.cms.modelmaker.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
	private final String column;
	private String onlyTable;
	private List<String> potentialTableList;
	private final String columnAlias;
	private List<Future<String>> futureDBValueList;
	private Map<String, String> variableAttrMap;
	private Map<String, String> AttrDomainDefMap;
	//private final String tableAlias;
	private Map<String, String> tableAliasMap;

	private static char Character = 'z';
	public CMSEntityEntry(String col){
		sqlElement = "";
		column = col.replaceFirst("\\(.+", "");
		/*String tempColumnAlias = col.replace(this.column+"(", "").replace(")", "");
		if(column.equals(tempColumnAlias)){
			columnAlias = "";
		}else{
			columnAlias = tempColumnAlias;
		}*/
		columnAlias = col.substring(column.length()).replaceAll("[\\(\\)]", "");
		onlyTable = "";
		//	tableAlias = "";
		potentialTableList = Collections.emptyList();
		futureDBValueList = Collections.emptyList();
		variableAttrMap =  Collections.emptyMap();
		AttrDomainDefMap =  Collections.emptyMap();
		tableAliasMap = Collections.emptyMap();

	}
	public synchronized CMSEntityEntry addFutureDBValue(Future<String> future) {
		if(futureDBValueList.isEmpty()){
			this.futureDBValueList = new ArrayList<>();
		}
		futureDBValueList.add(future);
		return this;
	}
	public CMSEntityEntry addToAttrDomainDefMap(String entityAttr,
			String domainDef) {
		if(AttrDomainDefMap.isEmpty()){
			AttrDomainDefMap = new Hashtable<>();
		}
		AttrDomainDefMap.put(entityAttr, domainDef);
		return this;
	}
	public CMSEntityEntry addToPotentialTableList(List<String> list) {
		if(potentialTableList.isEmpty()){
			potentialTableList = new ArrayList<>();
		}
		for (String string : list) {
			addToTableAliasMap(string);
			potentialTableList.add(string.replaceFirst("\\(.+", ""));
		}
		if(potentialTableList.size() == 1){
			onlyTable = potentialTableList.get(0);
		}
		return this;
	}
	/**
	 * claimcycle(CC) <=> claimcycle.claimcycleid = "CC"
	 */
	private void addToTableAliasMap(String tableString) {
		if(this.tableAliasMap.isEmpty()){
			this.tableAliasMap = new Hashtable<>();
		}
		this.tableAliasMap.put(tableString.replaceFirst("\\(.+", "")+"."+getColumn(), tableString.replaceFirst(".*\\(", "").replaceFirst("\\)", ""));
	}
	public CMSEntityEntry addToVariableAttrMap(String variable, String attr) {
		if(this.variableAttrMap.isEmpty()){
			this.variableAttrMap = new Hashtable<>();
		}
		//no need to lock, the order of this variable list
		// is not important ...
		this.variableAttrMap.put(variable + UNDERSCORE +nextCharacter(), attr);
		return this;
	}
	public String getAttribute(String variable) {
		return variableAttrMap.get(variable);
	}
	public String getColumn() {
		return column;
	}
	public String getColumnAlias() {
		return columnAlias;
	}
	public String getDomainDefinition() {
		try{
			return AttrDomainDefMap.values().toArray(new String[0])[0];
		}catch(Exception e){
			System.err.println("CMSEntityEntry.getDomainDefinition() throws an exception: "+ e.getMessage());
			return "";
		}
	}
	public String getDomainDefinition(String attribute) {
		return AttrDomainDefMap.get(attribute);
	}
	public String getEntityAttribute() {
		try{
			return AttrDomainDefMap.keySet().toArray(new String[0])[0];
		}catch(Exception e){
			System.err.println("CMSEntityEntry.getAttribute() throws an exception: "+ e.getMessage());
			return "";
		}
	}
	public Set<String> getEntityAttrSet() {
		return AttrDomainDefMap.keySet();
	}
	public List<Future<String>> getFutureDBReturnValueList(){
		return this.futureDBValueList;
	}
	public List<String> getPotentialTableList() {
		return potentialTableList;
	}
	public String getSqlElement() {
		return sqlElement;
	}
	public String getTable() {
		return onlyTable;
	}

	public String getTableAlias(String table) {
		return tableAliasMap.get(table+"."+getColumn());
	}

	public String getVariable(String variable) {
		for (String var : variableAttrMap.keySet()) {
			if(var.startsWith(variable+UNDERSCORE)) {
				return var;
			}
		}
		return "";
	}
	public Set<String> getVariableSet(){
		return this.variableAttrMap.keySet();
	}

	public boolean isFunction(){
		return onlyTable.startsWith("FUNCTION:");
	}
	/*public boolean isFunction() {
		return this.table.matches("FUNCTION:\\w+");
	}*/
	private synchronized char nextCharacter() {
		if(Character == 'z'){
			Character ='a';
		}
		return Character++;
	}
	public CMSEntityEntry setSqlElement(String sqlElement) {
		this.sqlElement = sqlElement;
		return this;
	}


}
