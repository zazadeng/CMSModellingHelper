/**
 * 
 */
package com.wcb.cms.modelmaker.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.datatools.modelbase.sql.query.PredicateBasic;
import org.eclipse.datatools.modelbase.sql.query.PredicateBetween;
import org.eclipse.datatools.modelbase.sql.query.PredicateInValueList;
import org.eclipse.datatools.modelbase.sql.query.PredicateInValueRowSelect;
import org.eclipse.datatools.modelbase.sql.query.PredicateQuantifiedRowSelect;
import org.eclipse.datatools.modelbase.sql.query.QueryCombined;
import org.eclipse.datatools.modelbase.sql.query.QueryExpressionBody;
import org.eclipse.datatools.modelbase.sql.query.QueryExpressionRoot;
import org.eclipse.datatools.modelbase.sql.query.QueryResultSpecification;
import org.eclipse.datatools.modelbase.sql.query.QuerySelect;
import org.eclipse.datatools.modelbase.sql.query.QuerySelectStatement;
import org.eclipse.datatools.modelbase.sql.query.QueryStatement;
import org.eclipse.datatools.modelbase.sql.query.QueryValueExpression;
import org.eclipse.datatools.modelbase.sql.query.QueryValues;
import org.eclipse.datatools.modelbase.sql.query.ResultColumn;
import org.eclipse.datatools.modelbase.sql.query.ResultTableAllColumns;
import org.eclipse.datatools.modelbase.sql.query.TableCorrelation;
import org.eclipse.datatools.modelbase.sql.query.TableExpression;
import org.eclipse.datatools.modelbase.sql.query.TableInDatabase;
import org.eclipse.datatools.modelbase.sql.query.TableJoined;
import org.eclipse.datatools.modelbase.sql.query.TableReference;
import org.eclipse.datatools.modelbase.sql.query.ValueExpressionCaseSearch;
import org.eclipse.datatools.modelbase.sql.query.ValueExpressionCaseSearchContent;
import org.eclipse.datatools.modelbase.sql.query.ValueExpressionCaseSimple;
import org.eclipse.datatools.modelbase.sql.query.ValueExpressionCaseSimpleContent;
import org.eclipse.datatools.modelbase.sql.query.ValueExpressionColumn;
import org.eclipse.datatools.modelbase.sql.query.ValueExpressionCombined;
import org.eclipse.datatools.modelbase.sql.query.ValueExpressionFunction;
import org.eclipse.datatools.modelbase.sql.query.ValueExpressionNested;
import org.eclipse.datatools.modelbase.sql.query.ValueExpressionScalarSelect;
import org.eclipse.datatools.modelbase.sql.query.ValueExpressionSimple;
import org.eclipse.datatools.modelbase.sql.query.ValueExpressionVariable;
import org.eclipse.datatools.modelbase.sql.query.util.SQLQuerySourceFormat;
import org.eclipse.datatools.modelbase.sql.statements.SQLStatement;
import org.eclipse.datatools.sqltools.parsers.sql.SQLParserException;
import org.eclipse.datatools.sqltools.parsers.sql.SQLParserInternalException;
import org.eclipse.datatools.sqltools.parsers.sql.query.SQLQueryParseResult;
import org.eclipse.datatools.sqltools.parsers.sql.query.SQLQueryParserManager;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import com.wcb.cms.modelmaker.api.ColumnInSQL;
import com.wcb.cms.modelmaker.api.SelectQueryReader;
import com.wcb.cms.modelmaker.api.TableInSQL;

import org.eclipse.datatools.sqltools.parsers.sql.query.SQLQueryParserManagerProvider;

public class SelectQueryReaderDTPPlugInImpl implements SelectQueryReader {

	
	private static final String kPlaceHolder = "PlaceHolder";

	private final SQLQueryParserManager parserManager;
	
	public SelectQueryReaderDTPPlugInImpl(){
		parserManager = SQLQueryParserManagerProvider.getInstance().getParserManager(null, null);
		
	}
	
	public Set getTableColumnsSet(String sql) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<TableInSQL> getTableSet(String sql) throws Exception{
		
		Set<TableInSQL> tables = new HashSet<TableInSQL>();
		//SQLQueryParserManager parserManager = org.eclipse.datatools.sqltools.parsers.sql.query.SQLQueryParserManagerProvider.getInstance().getParserManager(null, null);
		//Parse
        SQLQueryParseResult parseResult = parserManager.parseQuery(sql);
        
        QueryStatement resultObject = parseResult.getQueryStatement();
        QuerySelectStatement selectStmt = (QuerySelectStatement)resultObject;
    	QueryExpressionRoot queryExpr = selectStmt.getQueryExpr();
    	QueryExpressionBody queryExprBody = queryExpr.getQuery();
    	
    	for (Iterator<?> iterator = queryExprBody.eAllContents(); iterator.hasNext();) {
    		Object next = iterator.next();
    		//System.out.println(next);
    		if(next instanceof ValueExpressionScalarSelect){
    			ValueExpressionScalarSelect scalarSelect = (ValueExpressionScalarSelect)next;
    			addTableNameFrom(scalarSelect, tables);
    		}
    		TableInDatabase object = null;
    		try{
    			object = (TableInDatabase)next;
    			//System.out.println(object);
    			tables.add(new TableInSQL(object.getName(), object.getTableCorrelation().getName()));
    		}catch(NullPointerException e){
    			//can't get table correlation, ie, Alias
    			tables.add(new TableInSQL(object.getName()));
    		}catch(ClassCastException e){
    			//keep looping
    		}
    	}
        	
    	QuerySelect querySelect = getTopLevelQuerySelectObjectList(sql);
    	EList<?> list = querySelect.getFromClause();
    	for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Object object = (Object) iterator.next();
			if (object instanceof TableJoined) {
				TableJoined new_name = (TableJoined) object;
				
			}
			System.out.println(object);
		}
        return tables;
	}

	private void addTableNameFrom(ValueExpressionScalarSelect scalarSelect, Set<TableInSQL> tables) {
		for(Iterator<?> iter = scalarSelect.eAllContents(); iter.hasNext();){
			Object next = iter.next();
			if(next instanceof ValueExpressionScalarSelect){
				addTableNameFrom((ValueExpressionScalarSelect)next, tables);
			}
			TableInDatabase object = null;
			try{
				object = (TableInDatabase)next;
				tables.add(new TableInSQL(object.getName(), object.getTableCorrelation().getName()));
    		}catch(NullPointerException e){
    			//can't get table correlation, ie, Alias
    			tables.add(new TableInSQL(object.getName()));
    		}catch(ClassCastException e){
    			//keep looping
    		}
		}
		
	}

	public String getTopLevelFromItemString(String sql) throws SQLParserException, SQLParserInternalException {
        
        QuerySelect selectQuery = getTopLevelQuerySelectObjectList(sql);
        String stringForFirstFromItem =((TableReference)selectQuery.getFromClause().get(0)).getSQL();
        
		return stringForFirstFromItem == null ? "" : stringForFirstFromItem;
	}

	public List<String> getEqualToStringValueExprssionList(String sql) throws SQLParserException, SQLParserInternalException {
		List<String> expList = new ArrayList<String>();
		//SQLQueryParserManager parserManager = org.eclipse.datatools.sqltools.parsers.sql.query.SQLQueryParserManagerProvider.getInstance().getParserManager(null, null);
		//Parse
        SQLQueryParseResult parseResult = parserManager.parseQuery(sql);
        
        QueryStatement resultObject = parseResult.getQueryStatement();
        QuerySelectStatement selectStmt = (QuerySelectStatement)resultObject;
    	QueryExpressionRoot queryExpr = selectStmt.getQueryExpr();
    	QueryExpressionBody queryExprBody = queryExpr.getQuery();
    	//TreeIterator<EObject> elements = 
    	for (Iterator<?> iterator = queryExprBody.eAllContents(); iterator.hasNext();) {
    		Object next = iterator.next();
    		
    		try{
	    		if (next instanceof ValueExpressionSimple) {
					ValueExpressionSimple valExp = (ValueExpressionSimple) next;
					if(valExp.getValue().matches("'.*'")){
						String theSql = ((PredicateBasic)valExp.eContainer()).getSQL();
						expList.add(theSql);
					}
				}else if(next instanceof ValueExpressionVariable){
					ValueExpressionVariable valExpVar = (ValueExpressionVariable)next;
					String theSql = ((PredicateBasic)valExpVar.eContainer()).getSQL();
					//System.out.println(theSql);
					expList.add(theSql);
				}/*else{
					System.out.println(next);
				}*/
	    		
    		}catch(ClassCastException e){
    			//keep looping
    		}
    	}
		return expList;
	}
	
	public List<String> getInStringValueExprssionList(String sql)
			throws SQLParserException, SQLParserInternalException {
		List<String> expList = new ArrayList<String>();
		//SQLQueryParserManager parserManager = org.eclipse.datatools.sqltools.parsers.sql.query.SQLQueryParserManagerProvider.getInstance().getParserManager(null, null);
		//Parse
        SQLQueryParseResult parseResult = parserManager.parseQuery(sql);
        
        QueryStatement resultObject = parseResult.getQueryStatement();
        QuerySelectStatement selectStmt = (QuerySelectStatement)resultObject;
    	QueryExpressionRoot queryExpr = selectStmt.getQueryExpr();
    	QueryExpressionBody queryExprBody = queryExpr.getQuery();
    	//TreeIterator<EObject> elements = 
    	for (Iterator<?> iterator = queryExprBody.eAllContents(); iterator.hasNext();) {
    		Object next = iterator.next();
    		try{
	    		if (next instanceof PredicateInValueList) {
	    			PredicateInValueList inValueList = (PredicateInValueList) next;
					//System.out.println(inValueList.getSQL());
					expList.add(inValueList.getSQL());
				}
	    		
    		}catch(ClassCastException e){
    			//keep looping
    		}
    	}
		return expList;
	}
	
	public Set<ColumnInSQL> getFirstSelectItemsSet(String sql) throws SQLParserException, SQLParserInternalException {
		final String parsedSql = getParsedSQL(sql);
		Set<ColumnInSQL> selectItems = new LinkedHashSet<ColumnInSQL>();
		
		QuerySelect selectQuery = getTopLevelQuerySelectObjectList(parsedSql);
    	
		//Only take the "Fisrt" and "TOP LEVEL" select query
    	EList<?> selectClause = selectQuery.getSelectClause();
    	
    	//case: "Select *"
    	if(selectClause.size() == 0){
    		Set<ColumnInSQL> set = getTablesInFromClause(selectQuery);
    		selectItems.addAll(set);
    		return selectItems;
    	}
    	int counter = 0;
    	for (Iterator<?> iterator = selectClause.iterator(); iterator.hasNext();) {
			
    		Object next = iterator.next();
    		//System.out.println(next);
    		if (next instanceof ResultTableAllColumns) {
    			//Case: C.*
    			ResultTableAllColumns tableAllCols = (ResultTableAllColumns) next;
    			//System.out.println(tableAllCols.getTableExpr().getName());
				//System.out.println(tableAllCols.getName());
    			//selectItems.add(new ColumnInSQL(tableAllCols.getTableExpr().getName(),tableAllCols.getName(), "*"));
    			ColumnInSQL col = new ColumnInSQL();
    			col.tableName(tableAllCols.getTableExpr().getName()).tableAlias(tableAllCols.getName()).columnName("*").sql(tableAllCols.getSQL());
				selectItems.add(col);
			}else{
				//must be ResultColumn
				ResultColumn resultColumn = (ResultColumn)next;
				QueryValueExpression queryValueExpr = resultColumn.getValueExpr();
				String resultColumnAlias =  (resultColumn.getName() == null)?"":resultColumn.getName();
				String resultSql = resultColumn.getSQL();
				
				if (queryValueExpr instanceof ValueExpressionColumn) {
					//CASE: "C.claimCycleID", "claimCycleID",  "claimCycleID AS C"
					ValueExpressionColumn col = (ValueExpressionColumn) queryValueExpr;
					ColumnInSQL colInSQL = initColumnInSQLFromColumnExpr(col);
					colInSQL.columnAlias(resultColumnAlias)
							.sql(resultSql);
					selectItems.add(colInSQL);
	    			//selectItems.add(new ColumnInSQL(tableName, alias, col.getName(), resultColumnAlias));
				}else if (queryValueExpr instanceof ValueExpressionFunction) {
					ValueExpressionFunction func = (ValueExpressionFunction) queryValueExpr;
					ColumnInSQL colInSQL  = initColumnInSQLFromFunctionExpr(func);
					//counter++;
					//colInSQL.functionAliasName(resultColumnAlias+kPlaceHolder+counter) //add constant for uniqueness making sure the uniqueness of the place holders in INTO clause
					colInSQL.columnAlias(resultColumnAlias)
						.sql(resultSql);
					selectItems.add(colInSQL);
				}else if (queryValueExpr instanceof ValueExpressionScalarSelect) {
					//CASE: nested select
					
					//populate to table short name with the alias' name or else use function name in the nested select...
					ValueExpressionScalarSelect scalarSelect = (ValueExpressionScalarSelect)queryValueExpr;
					EList<?> nestedSelectClause = ((QuerySelect)scalarSelect.getQueryExpr().getQuery()).getSelectClause();
					for (Iterator<?> iterator2 = nestedSelectClause.iterator(); iterator2.hasNext();) {
						try{
							ResultColumn nestResultColumn = (ResultColumn) iterator2.next();
							QueryValueExpression nestedValueExpr = nestResultColumn.getValueExpr();
							if(nestedValueExpr instanceof ValueExpressionFunction){
								/*
								 * (SELECT MAX(applicWklyWRAmt) FROM WageRate) AS KKK
								 */
								ValueExpressionFunction func = (ValueExpressionFunction) nestedValueExpr;
								ColumnInSQL colInSQL  = initColumnInSQLFromFunctionExpr(func);
								colInSQL.columnAlias(resultColumnAlias)
										.sql(resultSql);
								
								selectItems.add(colInSQL);
								
							}else if(nestedValueExpr instanceof ValueExpressionColumn){
								/*
								 * (SELECT applicWklyWRAmt FROM WageRate) AS KKK
								 */
								ValueExpressionColumn col = (ValueExpressionColumn) nestedValueExpr;
								ColumnInSQL colInSQL = initColumnInSQLFromColumnExpr(col);
								colInSQL.columnName(colInSQL.getColumnName())
											.columnAlias(resultColumnAlias)
											.sql(resultSql);
								selectItems.add(colInSQL);
								
							}
						}catch(ClassCastException ee){
							// don't care about type: ResultTableAllColumns
						}
					}
					
				}else if(queryValueExpr instanceof ValueExpressionCaseSimple){
					/* ValueExpressionCaseSimple
					 * 
					 * CASE PDA.AEDMNTHLYERNGSCD
        				WHEN 'NET' THEN AECR.NEMTHAMT
        				WHEN 'GROSS' THEN AECR.GEADJSTDMTHLYAMT
      					END AS MONTHLYEARNINGS
					 */
					
					
					ValueExpressionCaseSimple caseSimpleExpr = (ValueExpressionCaseSimple)queryValueExpr;
					EList<?> contentList = caseSimpleExpr.getContentList();
					if(contentList.size()>0){
						ValueExpressionCaseSimpleContent firstContent = (ValueExpressionCaseSimpleContent) contentList.get(0);
						QueryValueExpression resultValueExpr = firstContent.getResultValueExpr();
						
						if(resultValueExpr instanceof ValueExpressionColumn){
							ValueExpressionColumn valueExprCol = (ValueExpressionColumn)resultValueExpr;
							ColumnInSQL colInSQL = initColumnInSQLFromColumnExpr(valueExprCol);
							colInSQL
							.columnAlias(resultColumnAlias)
							.sql(resultSql);
							selectItems.add(colInSQL);
						}else if (resultValueExpr instanceof ValueExpressionFunction){
							ValueExpressionFunction func = (ValueExpressionFunction) resultValueExpr;
							ColumnInSQL colInSQL  = initColumnInSQLFromFunctionExpr(func);
							colInSQL.columnAlias(resultColumnAlias)
								.sql(resultSql);
							selectItems.add(colInSQL);
						}else{
							System.out.println("NOT ValueExpressionColumn, instead it is "+resultValueExpr);
						}
						
					}
					
				}else if (queryValueExpr instanceof ValueExpressionCaseSearch){
					//ValueExpressionCaseSearch
					/*
					 *CASE
                      WHEN vrc.contributionEndDt IS NULL
                         THEN DATE('9999-12-31')
                      ELSE vrc.contributionEndDt
                      END AS contributionEndDt , 
					 */
					ValueExpressionCaseSearch caseSearchExpr = (ValueExpressionCaseSearch)queryValueExpr;
					EList<?> contentList = caseSearchExpr.getSearchContentList();
					if(contentList.size()>0){
						ValueExpressionCaseSearchContent firstContent = (ValueExpressionCaseSearchContent) contentList.get(0);
						QueryValueExpression resultValueExpr = firstContent.getValueExpr();
						
						if(resultValueExpr instanceof ValueExpressionColumn){
							ValueExpressionColumn valueExprCol = (ValueExpressionColumn)resultValueExpr;
							ColumnInSQL colInSQL = initColumnInSQLFromColumnExpr(valueExprCol);
							colInSQL
							.columnAlias(resultColumnAlias)
							.sql(resultSql);
							selectItems.add(colInSQL);
						}else if (resultValueExpr instanceof ValueExpressionFunction){
							ValueExpressionFunction func = (ValueExpressionFunction) resultValueExpr;
							ColumnInSQL colInSQL  = initColumnInSQLFromFunctionExpr(func);
							colInSQL.columnAlias(resultColumnAlias)
								.sql(resultSql);
							selectItems.add(colInSQL);
						}else{
							System.out.println("NOT ValueExpressionColumn, instead it is "+resultValueExpr);
						}
					}
					
				}else if(queryValueExpr instanceof ValueExpressionSimple){
					QueryValueExpression valueExpressionSimple = (ValueExpressionSimple) queryValueExpr;
					if(resultSql.matches(".*\\s*AS\\s*.*")){
						//e.g.: '1' AS SORT_ORDER
						ColumnInSQL colInSQL = new ColumnInSQL();
						colInSQL.columnAlias(resultSql.split("\\s*AS\\s*")[1].trim())
								.sql(resultSql);
						selectItems.add(colInSQL);
					}else{
						System.out.println("XXXXXXXXXXXX SOMETHING ELSE: "+resultColumn);
						ColumnInSQL colInSQL = new ColumnInSQL();
						colInSQL.columnAlias(kPlaceHolder+ counter++);
						selectItems.add(colInSQL);	
					}
				}else{
					System.out.println("XXXXXXXXXXXX SOMETHING ELSE: "+resultColumn);
					ColumnInSQL colInSQL = new ColumnInSQL();
					colInSQL.columnAlias(kPlaceHolder+ counter++);
					selectItems.add(colInSQL);	
				}
			}
    		
    	}
    	
		return selectItems;
	}

	private ColumnInSQL initColumnInSQLFromFunctionExpr(
			ValueExpressionFunction func) {
		
		EList<?> parameterList = func.getParameterList();
		ColumnInSQL dbCol = new ColumnInSQL();
		if(parameterList.size() == 0){
			//CASE: max(*) 
			//counter++;
			//dbCol = new ColumnInSQL(resultColumn.getSQL(), kALIAS+ counter, "");
			dbCol.functionName(func.getName());
				
			
		}else{
			for (Object object : parameterList) {
				if(object instanceof ValueExpressionSimple){
					//e.g.: DATE('9999-12-31')
					dbCol.functionName(func.getName());
				}else if (object instanceof ValueExpressionColumn) {
					//e.g.: DATE(PDCAL1.BUSINESSCALCDTM), DATE(PDCAL2.BUSINESSCALCDTM) AS SOM, DATE(BUSINESSCALCDTM)
					
					ValueExpressionColumn col = (ValueExpressionColumn) object;
					
					dbCol = initColumnInSQLFromColumnExpr(col);
					
					dbCol.functionName(func.getName());
					
				}
			}
		}
		return dbCol;
	}

	private ColumnInSQL initColumnInSQLFromColumnExpr(
			ValueExpressionColumn column) {
		//e.g.: PDCAL1.BUSINESSCALCDTM AS ALIAS
		TableExpression tableExp = column.getTableExpr();
		String tableName = "";
		String alias = "";
		if(tableExp == null){
			System.out.println("Check TableInDatabase: " + column.getTableInDatabase());
		}else{
			tableName = tableExp.getName();
			TableCorrelation tableAlias = tableExp.getTableCorrelation();
			if(tableAlias != null){
				alias = tableAlias.getName();
			}
		}
		ColumnInSQL colInSQL = new ColumnInSQL();
		colInSQL.tableName(tableName)
				.tableAlias(alias)
				.columnName(column.getName());
				
		return colInSQL;
	}

	private QuerySelect getTopLevelQuerySelectObjectList(String selectQuery)
			throws SQLParserException, SQLParserInternalException {
		
		//Parse
        SQLQueryParseResult parseResult = parserManager.parseQuery(selectQuery);
        
        QueryStatement resultObject = parseResult.getQueryStatement();
        QuerySelectStatement selectStmt = (QuerySelectStatement)resultObject;
    	QueryExpressionRoot queryExpr = selectStmt.getQueryExpr();
    	QueryExpressionBody queryExprBody = queryExpr.getQuery();
    	//List<QuerySelect> list =  new ArrayList<QuerySelect>();
    	if (queryExprBody instanceof QueryCombined) {
    		QueryCombined queryHasUnion = (QueryCombined) queryExprBody;
    		try{
    			return (QuerySelect)queryHasUnion.getLeftQuery();
    		}catch(ClassCastException e){
    			System.out.println("not select query but => "+queryHasUnion);
    		}
			
		}
    	return (QuerySelect)queryExprBody;
	}
	

	/*private void getQuerySelectObjectForCombinedQuery(QueryCombined queryHasUnion, List<QuerySelect> list) {
		QueryExpressionBody queryExpr = queryHasUnion.getLeftQuery();
		checkQueryExpressionForQuerySelectObjet(list, queryExpr);
		queryExpr = queryHasUnion.getRightQuery();
		checkQueryExpressionForQuerySelectObjet(list, queryExpr);
	}

	private void checkQueryExpressionForQuerySelectObjet(
			List<QuerySelect> list, QueryExpressionBody queryExpr) {
		if(queryExpr == null){
			//DO NOT need to continue to check the right query
			return;
		}else if(queryExpr instanceof QuerySelect){
			list.add((QuerySelect)queryExpr);
		}else if(queryExpr instanceof QueryCombined){
			getQuerySelectObjectForCombinedQuery((QueryCombined)queryExpr, list);
		}
	}*/

	private Set<ColumnInSQL> getTablesInFromClause(
			QuerySelect selectQuery) {
		/*TODO check how KKK is set ... and applicWklyWRAmt is set ...
		 * 
		 * select claimcycleid, applicWklyWRAmt 
		 *	from claimcycle, 
		 * 	(SELECT applicWklyWRAmt, versionNO FROM WageRate) KKK 
		 */
		Set<ColumnInSQL> selectItems = new LinkedHashSet<ColumnInSQL>();
		EList<?> list = selectQuery.getFromClause();
		for (Iterator<?> iterator = list.iterator(); iterator.hasNext();) {
			TableReference ref = (TableReference) iterator.next();
			if(ref instanceof TableInDatabase){
				TableInDatabase table = (TableInDatabase)ref;
				ColumnInSQL col = new ColumnInSQL();
				col.tableName(table.getName()).tableAlias(table.getTableCorrelation().getName()).columnName("*");
				selectItems.add(col);
				//selectItems.add(new ColumnInSQL(table.getName(),table.getTableCorrelation().getName(), "*"));
			}else{
				for (Iterator<?> iterator2 = ref.eAllContents(); iterator2.hasNext();) {
				
					Object object = (Object)iterator2.next() ;
					if( object instanceof TableInDatabase){
						TableInDatabase table = (TableInDatabase)object;
						ColumnInSQL col = new ColumnInSQL();
						col.tableName(table.getName()).tableAlias(table.getTableCorrelation().getName()).columnName("*");
						selectItems.add(col);
						//selectItems.add(new ColumnInSQL(table.getName(),table.getTableCorrelation().getName(), "*"));
						//System.out.println(object);
					}
				
				}
			}
		}
		return selectItems;
	}
	
	public String getParsedSQL(String sql) throws SQLParserException, SQLParserInternalException {
		
		return parserManager.parseQuery(sql).getQueryStatement().getSQL();
	}

	@Override
	public List<String> getBetweenStringValueExprssionList(String sql)
			throws Exception {

		List<String> expList = new ArrayList<String>();
		//SQLQueryParserManager parserManager = org.eclipse.datatools.sqltools.parsers.sql.query.SQLQueryParserManagerProvider.getInstance().getParserManager(null, null);
		//Parse
        SQLQueryParseResult parseResult = parserManager.parseQuery(sql);
        
        QueryStatement resultObject = parseResult.getQueryStatement();
        QuerySelectStatement selectStmt = (QuerySelectStatement)resultObject;
    	QueryExpressionRoot queryExpr = selectStmt.getQueryExpr();
    	QueryExpressionBody queryExprBody = queryExpr.getQuery();
    	//TreeIterator<EObject> elements = 
    	for (Iterator<?> iterator = queryExprBody.eAllContents(); iterator.hasNext();) {
    		Object next = iterator.next();
    		
    		try{
	    		if(next instanceof ValueExpressionVariable){
					ValueExpressionVariable valExpVar = (ValueExpressionVariable)next;
					String theSql = ((PredicateBetween)valExpVar.eContainer()).getSQL();
					//System.out.println(theSql);
					expList.add(theSql);
				}/*else{
					System.out.println(next);
				}*/
	    		
    		}catch(ClassCastException e){
    			//keep looping
    		}
    	}
		return expList;
	}
}
