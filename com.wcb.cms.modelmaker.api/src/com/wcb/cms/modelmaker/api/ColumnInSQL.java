package com.wcb.cms.modelmaker.api;


public class ColumnInSQL{
	private String columnName;
	/*private String tableShortName;
	private String tableName;*/
	private TableInSQL tableInSQL;
	//private boolean functionFlag;
	private String functionName;
	private String columnAliasName;
	private String sql;
	
	//private ColumnInSQL thisObject;
	//private String functionAliasName;
	
	public ColumnInSQL(){
		columnName = "";
		tableInSQL = new TableInSQL();
		functionName = "";
		columnAliasName = "";
		//functionAliasName = "";
		sql = "";
	}
	/*public ColumnInSQL(String column) {
		//"claimcycleID"
		this("", "", column, "");
	}

	public ColumnInSQL(String alias, String column) {
		//"cc.claimcycleID"
		this("", alias, column, "");
	}
	
	public ColumnInSQL(String tableName, String alias, String column) {
		this(tableName, alias, column, "");
	}

	public ColumnInSQL(String tableName, String alias, String column, String functionName) {
		this(tableName, alias, column, "", functionName, "");
	}
	
	public ColumnInSQL(String tableName, String alias, String column, String columnAlias, String functionName, String sql) {
		tableInSQL = new TableInSQL(tableName, alias);
		columnName = column;
		this.functionName = functionName;
		columnAliasName = columnAlias;
		this.sql = sql;
	}*/
	
	public ColumnInSQL columnName(String name){
		this.columnName = name;
		return this;
	}
	
	public ColumnInSQL columnAlias(String name){
		this.columnAliasName = name;
		return this;
	}

	public ColumnInSQL tableAlias(String name){
		if(this.tableInSQL == null){
			this.tableInSQL = new TableInSQL("", name);
		}else{
			this.tableInSQL.setAlias(name);
		}
		return this;
	}
	
	public ColumnInSQL tableName(String name){
		if(this.tableInSQL == null){
			this.tableInSQL = new TableInSQL(name);	
		}else{
			this.tableInSQL.setName(name);
		}
		
		return this;
	}
	
	public ColumnInSQL functionName(String name){
		this.functionName = name;
		return this;
	}
	
	/*public ColumnInSQL functionAliasName(String name){
		if(thisObject == null){
			thisObject = new ColumnInSQL();
		}
		thisObject.functionAliasName = name;
		if(thisObject.sql.equals("")){
			thisObject.sql = name;
		}
		return thisObject;
	}*/
	
	/*public ColumnInSQL functionColumn(String name){
		//grab a column inside a function ... for obtaining domain definition
		if(thisObject == null){
			thisObject = new ColumnInSQL();
		}
		thisObject.columnName = name;
		return thisObject;
	}*/
	
	/*public ColumnInSQL functionColumn(String column, String table, String tableAlias){
		if(thisObject == null){
			thisObject = new ColumnInSQL();
		}

		thisObject.tableInSQL = new TableInSQL(table, tableAlias);
		thisObject.columnName = column;
		
		return thisObject;
	}*/
	
	/*public String getFunctionColumnName(){
		if(thisObject.functionName.equals("")){
			return "";
		}
		return thisObject.columnName;
	}
	
	public String getFunctionColumnTableName(){
		if(thisObject.functionName.equals("")){
			return "";
		}
		return thisObject.tableInSQL.getName();
	}

	public String getFunctionColumnTableAliasName(){
		if(thisObject.functionName.equals("")){
			return "";
		}
		return thisObject.tableInSQL.getAlias();
	}*/
	
	/*public String getFunctionAliasName(){
		return thisObject.functionAliasName;
	}*/
	
	public ColumnInSQL sql(String name){
		this.sql = name;
		return this;
	}
	
	public String getColumnName() {
		return columnName;
	}
	
	public String getTableName() {
		return tableInSQL.getName();
	}
	
	public String getTableShortName() {
		return tableInSQL.getAlias();
	}
	
	public String getColumnAlias(){
		return columnAliasName;
	}
	
	@Override
	public String toString() {
		/*if(isFunctionColumn()){
			return this.getTableName();
		}
		if(tableInSQL.toString().equals("")){
			return columnName;
		}
		return tableInSQL + 
				(columnName.equals("")?"":"." +columnName) +
				(columnAliasName.equals("")?"":" AS " +columnAliasName);*/
		return this.sql;
	}

	
	/*public void isFunctionColumn(boolean yesOrNo) {
		functionFlag = yesOrNo;
	}*/
	
	public boolean isFunctionColumn() {
		return functionName.length()>0;
	}
	
	@Override
	public boolean equals(Object obj) {
		try{
			ColumnInSQL col = (ColumnInSQL)obj;
			
			/*return this.columnAliasName.equalsIgnoreCase(col.columnAliasName) 
					&& this.functionName.equalsIgnoreCase(col.functionName) 
					&& this.columnName.equalsIgnoreCase(col.columnName) 
					&& this.tableInSQL.equals(col.tableInSQL);*/
			return sql.equalsIgnoreCase(col.sql) && !this.sql.equals("");
		}catch (ClassCastException e) {
			//return false later...
		}
		return false;
	}

	
	/*public void setFunctionColumnName(String name) {
		this.functionName = name;
	}
*/
	public String getFunctionName(){
		return functionName;
	}
	
	public String getSQL(){
		return sql;
	}
}
