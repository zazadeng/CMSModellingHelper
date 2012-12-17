package com.wcb.cms.modelmaker.api;

public class TableInSQL {

	private String name;
	private String alias;
	
	public TableInSQL(String tableName, String tableAlias){
		this.name = tableName;
		this.alias = tableAlias;
	}
	
	public TableInSQL(String tableName) {
		this(tableName, "");
	}

	public TableInSQL() {
		this("");
	}

	public String getAlias(){
		return alias;
	}
	
	public String getName(){
		return name;
	}

	public void setAlias(String tableAlias) {
		this.alias = tableAlias; 
	}
	
	public void setName(String tableName){
		this.name = tableName;
	}
	
	@Override
	public boolean equals(Object obj) {
		try{
			TableInSQL tab = (TableInSQL)obj;
			return this.name.equalsIgnoreCase(tab.name) 
				&& this.alias.equalsIgnoreCase(tab.alias);
		}catch (ClassCastException e) {
			//return false later ...
		}
		return false;
	}
	
	@Override
	public String toString() {
		if(this.alias != null && this.alias.equals("")){
			if(this.name != null && this.name.equals("")){
				return "";
			}
			return this.name;
		}
		return this.alias;
	}
}
