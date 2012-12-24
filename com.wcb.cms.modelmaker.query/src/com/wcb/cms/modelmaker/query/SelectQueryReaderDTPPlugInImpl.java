/**
 * 
 */
package com.wcb.cms.modelmaker.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.datatools.modelbase.sql.datatypes.PredefinedDataType;
import org.eclipse.datatools.modelbase.sql.query.PredicateBasic;
import org.eclipse.datatools.modelbase.sql.query.PredicateBetween;
import org.eclipse.datatools.modelbase.sql.query.PredicateInValueList;
import org.eclipse.datatools.modelbase.sql.query.QueryCombined;
import org.eclipse.datatools.modelbase.sql.query.QueryExpressionBody;
import org.eclipse.datatools.modelbase.sql.query.QueryExpressionRoot;
import org.eclipse.datatools.modelbase.sql.query.QueryResultSpecification;
import org.eclipse.datatools.modelbase.sql.query.QuerySelect;
import org.eclipse.datatools.modelbase.sql.query.QuerySelectStatement;
import org.eclipse.datatools.modelbase.sql.query.QueryStatement;
import org.eclipse.datatools.modelbase.sql.query.QueryValueExpression;
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
import org.eclipse.datatools.modelbase.sql.query.ValueExpressionCast;
import org.eclipse.datatools.modelbase.sql.query.ValueExpressionColumn;
import org.eclipse.datatools.modelbase.sql.query.ValueExpressionFunction;
import org.eclipse.datatools.modelbase.sql.query.ValueExpressionScalarSelect;
import org.eclipse.datatools.modelbase.sql.query.ValueExpressionSimple;
import org.eclipse.datatools.modelbase.sql.query.ValueExpressionVariable;
import org.eclipse.datatools.sqltools.parsers.sql.SQLParserException;
import org.eclipse.datatools.sqltools.parsers.sql.SQLParserInternalException;
import org.eclipse.datatools.sqltools.parsers.sql.query.SQLQueryParseResult;
import org.eclipse.datatools.sqltools.parsers.sql.query.SQLQueryParserManager;
import org.eclipse.datatools.sqltools.parsers.sql.query.SQLQueryParserManagerProvider;
import org.eclipse.emf.common.util.EList;

import com.wcb.cms.modelmaker.api.ColumnInSQL;
import com.wcb.cms.modelmaker.api.SelectQueryReader;
import com.wcb.cms.modelmaker.api.TableInSQL;

public final class SelectQueryReaderDTPPlugInImpl implements SelectQueryReader {


	private static final String kColon = ":";
	private static final String kEquals = "=";
	private static final String kNotEquals = "<>";
	private static final String kDomainFollows = "@";
	private static final String kColumnAlaisOrTableAlaisFollows = "_";
	private static final String kInvalidAttribute = "INVALID_ATTRIBUTE";
	private static final String kPlaceHolder = "PlaceHolder";

	private final static SQLQueryParserManager parserManager = SQLQueryParserManagerProvider.getInstance().getParserManager(null, null);
	private static final String STAR = "*";

	public SelectQueryReaderDTPPlugInImpl(){


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

	private List<String> collectTableNameIn(TableJoined tableJoined, String columnName) {
		List<String> list  = new ArrayList<String>(2);
		/*
		 * RIGHT
		 */
		TableReference tableRefRight = tableJoined.getTableRefRight();
		if(tableRefRight instanceof TableInDatabase){
			list.add(findTableNameIn((TableInDatabase)tableRefRight, columnName));
		}else if(tableRefRight instanceof QuerySelect){
			list.addAll(findTableNameIn((QuerySelect)tableRefRight, columnName));
		}

		/*
		 * LEFT
		 */
		TableReference tableRefLeft = tableJoined.getTableRefLeft();
		if(tableRefLeft instanceof TableInDatabase){
			list.add(findTableNameIn((TableInDatabase)tableRefLeft, columnName));
		}else if(tableRefLeft instanceof QuerySelect){
			list.addAll(findTableNameIn((QuerySelect)tableRefLeft, columnName));
		}

		return list;
	}

	@Override
	public String composeCuramNonStandardSelectQuery(String selectQuery,
			List<Map<String, String>> intoStatementMetaData,
			List<Map<String, String>> constAndVarialbeMetaDataList) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String composeSelectQuery(List<Map<String, String>> intoClauseList,
			List<Map<String, String>> constAndVarialbeList) {
		// TODO Auto-generated method stub
		return null;
	}

	private Map<? extends String, ? extends List<String>> findColumnTableMapInValueExpressionCaseSimpleFrom(
			QueryValueExpression valueExpr, String alias) {
		try{
			final ValueExpressionCaseSimple valueExpressionCaseSimple = (ValueExpressionCaseSimple)valueExpr;

			for (Iterator<?> iterator = valueExpressionCaseSimple.getContentList().iterator(); iterator
					.hasNext();) {
				//ONLY needs the first one to get the table name
				QueryValueExpression resultValueExpr = ((ValueExpressionCaseSimpleContent) iterator.next()).getResultValueExpr();
				return Collections.singletonMap(resultValueExpr.getName() + alias, findTableNameInValueExpressionColumnFrom(resultValueExpr));

				/*try{
					ValueExpressionColumn resultValueExprCol = (ValueExpressionColumn)resultValueExpr;
					return Collections.singletonMap(resultValueExpr.getName() + alias, findTableNameIn(resultValueExprCol));
				}catch(ClassCastException ee){

				}*/

			}
		}catch(ClassCastException e){
			//that is fine ...
		}
		return Collections.emptyMap();
	}

	private Map<? extends String, ? extends List<String>> findColumnTableMapInValueExpressionCastFrom(
			QueryValueExpression valueExpr, String alias) {
		try{
			final ValueExpressionCast cast  = (ValueExpressionCast)valueExpr;
			final String dataType = ((PredefinedDataType)cast.getDataType()).getPrimitiveType().getName();
			return Collections.singletonMap(dataType+alias, Collections.singletonList("FUNCTION:CAST"));
		}catch(ClassCastException e){
			//that is fine ...
		}
		return Collections.emptyMap();
	}


	private Map<String, List<String>> findColumnTableMapInValueExpressionColumnFrom(
			QueryValueExpression valueExpr, String alias) {
		try{
			ValueExpressionColumn valueExpressionColumn = (ValueExpressionColumn)valueExpr;
			return Collections.singletonMap(valueExpr.getName() + alias, lookUpValueExpressionColumn(valueExpressionColumn));
		}catch(ClassCastException e){
			//that is fine ...
		}
		return Collections.emptyMap();
	}

	private Map<? extends String, ? extends List<String>> findColumnTableMapInValueExpressionFunctionFrom(
			QueryValueExpression valueExpr, String alias) {
		try{
			final ValueExpressionFunction valueExpressionFunction = (ValueExpressionFunction)valueExpr;

			final String functionName = valueExpressionFunction.getName();
			final List<String> singletonList = Collections.singletonList("FUNCTION:"+functionName);
			if(!alias.isEmpty()){
				return Collections.singletonMap(functionName+alias, singletonList);
			}
			final EList<?> parameterList = valueExpressionFunction.getParameterList();
			if(parameterList.isEmpty()){
				return Collections.singletonMap(functionName, singletonList);
			}
			for (Iterator<?> iterator = parameterList.iterator(); iterator
					.hasNext();) {
				ValueExpressionColumn valueExpressionColumn = (ValueExpressionColumn) iterator.next();
				final String varExprColName = valueExpressionColumn.getName();
				if(varExprColName != null){
					return Collections.singletonMap(varExprColName+"_"+functionName, singletonList);
				}

			}

		}catch(ClassCastException e){
			//that is fine ...
		}
		return Collections.emptyMap();
	}

	private List<String> findTableNameIn(
			QuerySelect selectQueryObject, String columnName) {

		EList<?> fromClauseList = selectQueryObject.getFromClause();
		List<String> list = new ArrayList<String>(fromClauseList.size());
		for (Iterator<?> iterator = fromClauseList.iterator(); iterator
				.hasNext();) {
			TableReference tableRef = (TableReference) iterator.next();
			if (tableRef instanceof TableInDatabase) {
				list.add(findTableNameIn((TableInDatabase) tableRef, columnName));
			}else if(tableRef instanceof TableJoined){
				list.addAll(collectTableNameIn((TableJoined)tableRef, columnName));
			}else if(tableRef instanceof QuerySelect){
				list.addAll(findTableNameIn((QuerySelect)tableRef, columnName));
			}
		}
		return list;
	}

	private Map<? extends String, ? extends List<String>> findTableNameIn(
			ResultColumn oneResultColumn) {

		final QueryValueExpression valueExpr = oneResultColumn.getValueExpr();
		final String alias = (oneResultColumn.getName() == null)?"":"(" +oneResultColumn.getName() +")";



		try{
			ValueExpressionColumn valueExpressionColumn = (ValueExpressionColumn) valueExpr;
			return Collections.singletonMap(valueExpr.getName() + alias,findTableNameIn(valueExpressionColumn));
		}catch(ClassCastException e){
			try{
				ValueExpressionCaseSimple valueExpressionCaseSimple = (ValueExpressionCaseSimple)valueExpr;
				for (Iterator<?> iterator = valueExpressionCaseSimple.getContentList().iterator(); iterator
						.hasNext();) {
					//ONLY needs the first one to get the table name
					QueryValueExpression resultValueExpr = ((ValueExpressionCaseSimpleContent) iterator.next()).getResultValueExpr();
					try{
						ValueExpressionColumn resultValueExprCol = (ValueExpressionColumn)resultValueExpr;
						return Collections.singletonMap(resultValueExpr.getName() + alias, findTableNameIn(resultValueExprCol));
					}catch(ClassCastException ee){

					}

				}
			}catch(ClassCastException ee){
				try{
					ValueExpressionFunction valueExpressionFunction = (ValueExpressionFunction)valueExpr;
					final String functionName = valueExpressionFunction.getName();
					final List<String> singletonList = Collections.singletonList("FUNCTION:"+functionName);
					//final String name = resultColumn.getName();
					if(!alias.isEmpty()){
						return Collections.singletonMap(functionName+alias, singletonList);
					}
					EList<?> parameterList = valueExpressionFunction.getParameterList();
					if(parameterList.isEmpty()){
						return Collections.singletonMap(functionName, singletonList);
					}
					for (Iterator<?> iterator = parameterList.iterator(); iterator
							.hasNext();) {
						ValueExpressionColumn valueExpressionColumn = (ValueExpressionColumn) iterator.next();
						final String varExprColName = valueExpressionColumn.getName();
						if(varExprColName != null){
							return Collections.singletonMap(varExprColName+"_"+functionName, singletonList);
						}

					}

				}catch(ClassCastException eee){
					//MAYBE something else
				}
			}

		}
		return Collections.emptyMap();
	}

	private String findTableNameIn(TableInDatabase tableInDB, String columnName) {
		if(isStar(columnName)){
			return tableInDB.getName();
		}
		EList<?> valueExprColumns = tableInDB.getValueExprColumns();
		for (Iterator<?> iterator = valueExprColumns.iterator(); iterator
				.hasNext();) {
			ValueExpressionColumn column = (ValueExpressionColumn) iterator.next();
			if(column.getName().equals(columnName)){
				return tableInDB.getName();
			}

		}
		//potential table, add a ? mark at the end
		return tableInDB.getName() + "?";
	}

	private List<String> findTableNameIn(ValueExpressionColumn valueExprColumn) {
		try{
			TableInDatabase tableInDatabase = (TableInDatabase)valueExprColumn.getTableExpr();
			return Collections.singletonList(findTableNameIn(tableInDatabase, valueExprColumn.getName()));

		}catch(ClassCastException e){
			try{
				QuerySelect querySelect = (QuerySelect)valueExprColumn.getTableExpr();
				return findTableNameIn(querySelect, valueExprColumn.getName());

			}catch(ClassCastException ee){

			}
		}
		return null;
	}

	private List<String> findTableNameInQuerySelectFrom(
			TableExpression tableExpr, String theColumnToFindTable) {
		try{
			QuerySelect querySelect = (QuerySelect)tableExpr;
			return lookUpQuerySelect(querySelect, theColumnToFindTable, null);
		}catch(ClassCastException ee){
			return Collections.emptyList();
		}
	}

	private List<String> findTableNameInQuerySelectFrom(
			TableReference tableRef, String columnToFindTable, Map<String, List<String>> map) {
		try{
			QuerySelect querySelect = (QuerySelect)tableRef;
			return lookUpQuerySelect(querySelect, columnToFindTable, map);
		}catch(ClassCastException e){
			return Collections.emptyList();
		}
	}

	private List<String> findTableNameInTableInDatabaseFrom(
			TableExpression tableExpr, String theColumnToFindTable) {
		try{
			final TableInDatabase tableInDatabase = (TableInDatabase)tableExpr;
			return lookUpTableInDatabase(tableInDatabase, theColumnToFindTable);
		}catch(ClassCastException e){
			return Collections.emptyList();
		}

	}

	private List<String> findTableNameInTableInDatabaseFrom(TableReference tableRef,
			String columnToFindTable) {
		try{
			final TableInDatabase tableInDB = (TableInDatabase)tableRef;
			return lookUpTableInDatabase(tableInDB, columnToFindTable);
		}catch(ClassCastException e){
			return Collections.emptyList();
		}
	}

	private List<String> findTableNameInTableJoinedFrom(
			TableReference tableRef, String columnToFindTable, Map<String, List<String>> map) {
		try{
			TableJoined tableJoined = (TableJoined)tableRef;
			List<String> list = new ArrayList<String>();
			/*
			 * RIGHT
			 */
			TableReference tableRefRight = tableJoined.getTableRefRight();
			list.addAll(findTableNameInTableInDatabaseFrom(tableRefRight, columnToFindTable));//TableInDatabase
			list.addAll(findTableNameInQuerySelectFrom(tableRefRight, columnToFindTable, map));//QuerySelect

			/*
			 * LEFT
			 */
			TableReference tableRefLeft = tableJoined.getTableRefLeft();
			list.addAll(findTableNameInTableInDatabaseFrom(tableRefLeft, columnToFindTable));//TableInDatabase
			list.addAll(findTableNameInQuerySelectFrom(tableRefLeft, columnToFindTable, map));//QuerySelect

			return list;
		}catch(ClassCastException e){
			return Collections.emptyList();
		}

	}

	private List<String> findTableNameInValueExpressionColumnFrom(
			QueryValueExpression valueExpr) {
		try{
			ValueExpressionColumn valueExpressionColumn = (ValueExpressionColumn) valueExpr;

			return lookUpValueExpressionColumn(valueExpressionColumn);
		}catch(ClassCastException e){
			return Collections.emptyList();
		}

		/*try{
			TableInDatabase tableInDatabase = (TableInDatabase)valueExprColumn.getTableExpr();
			return Collections.singletonList(findTableNameIn(tableInDatabase, valueExprColumn.getName()));

		}catch(ClassCastException e){
			try{
				QuerySelect querySelect = (QuerySelect)valueExprColumn.getTableExpr();
				return findTableNameIn(querySelect, valueExprColumn.getName());

			}catch(ClassCastException ee){

			}
		}
		return null;*/
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

	@Override
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

	private QuerySelect getFirstQuerySelect(String sql)
			throws SQLParserException, SQLParserInternalException {


		//Parse
		SQLQueryParseResult parseResult = parserManager.parseQuery(sql);

		QueryStatement resultObject = parseResult.getQueryStatement();
		QuerySelectStatement selectStmt = (QuerySelectStatement)resultObject;
		QueryExpressionRoot queryExpr = selectStmt.getQueryExpr();
		QueryExpressionBody queryExprBody = queryExpr.getQuery();

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

	@Override
	public Set<ColumnInSQL> getFirstSelectItemsSet(String sql) throws SQLParserException, SQLParserInternalException {
		//final String parsedSql = getParsedSQL(sql);
		Set<ColumnInSQL> selectItems = new LinkedHashSet<ColumnInSQL>();

		QuerySelect selectQuery = getFirstQuerySelect(sql);

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
					//QueryValueExpression valueExpressionSimple = queryValueExpr;
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

	@Override
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

	@Override
	public String getParsedSQL(String sql) throws SQLParserException, SQLParserInternalException {

		return parserManager.parseQuery(sql).getQueryStatement().getSQL();
	}

	@Override
	public String getSelectAndIntoClauses(String selectQuery) throws Exception{

		final Set<ColumnInSQL> selectItems = getFirstSelectItemsSet(selectQuery);

		List<String> records = null;
		final Map<String,String> columnMap = new HashMap<String,String>(selectItems.size());

		for (Iterator<ColumnInSQL> iterator = selectItems.iterator(); iterator.hasNext();) {
			ColumnInSQL selectItem =  iterator.next();
			String fullTableName = selectItem.getTableName();
			String displayValue = selectItem.getColumnAlias().equals("") ? selectItem.getTableShortName() :selectItem.getColumnAlias();

			if(isAllTableColumns(selectItem)){
				//aliasString = tableShortName;
				//fullTableName =
				if(!fullTableName.equals("")){

					/*	records = this.getRecords(new Object[]{fullTableName});
					for (Iterator<String> iterator2 = records.iterator(); iterator2
							.hasNext();) {
						String record = iterator2.next();
						String[] values = record.split(kColon);
						String oneIntoColumn = values[1]+ kColumnAlaisOrTableAlaisFollows +
								displayValue+ kDomainFollows + values[2];
						columnMap.put(selectItem.getSQL() , oneIntoColumn);
					}*/

				}
			}else if(isTableColumn(selectItem)){
				if(!fullTableName.equals("")){

					/*records = this.getRecords(new Object[]{
							fullTableName,
							selectItem.getColumnName()
					});
					//should be singleton list
					try{
						String[] values = records.get(0).split(kColon);

						String oneIntoColumn = values[1]+ kColumnAlaisOrTableAlaisFollows +
								displayValue+ kDomainFollows + values[2];
						columnMap.put(selectItem.getSQL(), oneIntoColumn);
					}catch(IndexOutOfBoundsException e){
						throw new TableValueNotFoundException("Missing "+"TABLE:<"+fullTableName+"> or COLUMN:<"+selectItem.getColumnName()+">");
					}
					 */

				}
			}else if(selectItem.isFunctionColumn()){
				String intoVariableStr = "";
				String domainDef = "";
				String columnName = selectItem.getColumnName();
				if(columnName.equals("")){
					if(selectItem.getFunctionName().equalsIgnoreCase("date")){
						// DATE('9999-12-31')
						intoVariableStr = selectItem.getFunctionName().toLowerCase()+ kColumnAlaisOrTableAlaisFollows +
								selectItem.getColumnAlias()+ kDomainFollows + "CURAM_DATE";
						columnMap.put(selectItem.getSQL(), intoVariableStr);
					}else{
						// max(), max(*),  count(), count(*), the following belongs to a different category: max(1),max(claimcycleid)...
						intoVariableStr = selectItem.getFunctionName().toLowerCase()+ kColumnAlaisOrTableAlaisFollows +
								selectItem.getColumnAlias()+ kDomainFollows + "SVR_INT64";
						columnMap.put(selectItem.getSQL(), intoVariableStr);
					}
				}else{
					if(selectItem.getFunctionName().equalsIgnoreCase("date")){
						domainDef = "CURAM_DATE";
						intoVariableStr = columnName + kColumnAlaisOrTableAlaisFollows + displayValue + kDomainFollows + domainDef;
						columnMap.put(selectItem.getSQL(), intoVariableStr);
					}else if(selectItem.getFunctionName().equalsIgnoreCase("count")){
						domainDef = "COUNT";
						intoVariableStr = columnName + kColumnAlaisOrTableAlaisFollows + displayValue + kDomainFollows + domainDef;
						columnMap.put(selectItem.getSQL(), intoVariableStr);
					}else{
						/*records = this.getRecords(new Object[]{
								fullTableName,
								columnName
						});
						//should be singleton list
						String[] values = records.get(0).split(kColon);
						domainDef = values[2];
						String oneIntoColumn = values[1]+ kColumnAlaisOrTableAlaisFollows +
								displayValue+ kDomainFollows + domainDef;
						columnMap.put(selectItem.getSQL(), oneIntoColumn);*/
					}
				}

			}else{
				//intoRecordsList.add(selectItem.getColumnName().toLowerCase() + "@NEED_HUMAN_INPUT");
				columnMap.put(selectItem.getSQL(), displayValue + "@NEED_HUMAN_INPUT");
			}
		}

		final String tab = "\n,	";

		String selectClause = "SELECT";
		Set<String> selectList = columnMap.keySet();

		for (Iterator<String> iterator = selectList.iterator(); iterator.hasNext();) {
			selectClause += tab+ iterator.next()  ;
		}
		selectClause = selectClause.replaceFirst(",", "");
		String intoClause = "\nINTO";
		for (Iterator<String> iterator = selectList.iterator(); iterator.hasNext();) {
			String columnInSQL = iterator.next();
			intoClause += tab + kColon + columnMap.get(columnInSQL);
		}
		intoClause = intoClause.replaceFirst(",", "");
		return selectClause+ intoClause;

	}

	@Override
	public Set getTableColumnsSet(String sql) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
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

		QuerySelect querySelect = getFirstQuerySelect(sql);
		EList<?> list = querySelect.getFromClause();
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Object object = iterator.next();
			if (object instanceof TableJoined) {
				TableJoined new_name = (TableJoined) object;

			}
			System.out.println(object);
		}
		return tables;
	}

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

					Object object = iterator2.next() ;
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

	@Override
	public String getTopLevelFromItemString(String sql) throws SQLParserException, SQLParserInternalException {

		QuerySelect selectQuery = getFirstQuerySelect(sql);
		String stringForFirstFromItem =((TableReference)selectQuery.getFromClause().get(0)).getSQL();

		return stringForFirstFromItem == null ? "" : stringForFirstFromItem;
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

	private boolean isAllTableColumns(ColumnInSQL selectItem) {
		//e.g.: "C.*" or "*"
		return (	!("").equals(selectItem.getTableShortName())
				&& 	!("").equals(selectItem.getTableName())
				&& ("*").equals(selectItem.getColumnName())
				&& !selectItem.isFunctionColumn() );
	}

	private boolean isStar(String columnName) {
		return columnName.equals(STAR);
	}

	private boolean isTableColumn(ColumnInSQL selectItem) {
		//e.g.: C.claimCycleID
		return (selectItem.getColumnName()!=null) && !selectItem.getColumnName().equals("*")
				&& (selectItem.getTableName()!=null) && !selectItem.getTableName().equals("")
				&& (selectItem.getTableShortName() !=null) && !selectItem.getTableShortName().equals("")
				&& (selectItem.isFunctionColumn() == false);
	}

	private List<String> lookUpQuerySelect(QuerySelect selectQueryObject,
			String theColumnToFindTable,
			Map<String, List<String>> map) {
		EList<?> selectClauseColumns = selectQueryObject.getSelectClause();

		if(selectClauseColumns.isEmpty()){
			return lookUpTheFromClause(selectQueryObject, theColumnToFindTable, map);
		}

		//selectClauseColumns is NOT empty but
		if(isStar(theColumnToFindTable)){
			for (Iterator<?> iterator = selectClauseColumns.iterator(); iterator
					.hasNext();) {
				final QueryResultSpecification queryResultSpec = (QueryResultSpecification) iterator.next();
				final String name = ((ResultColumn)queryResultSpec).getValueExpr().getName();
				map.put(name, lookUpTheFromClause(selectQueryObject, name, map));
			}
		}

		//theColumnToFindTable is not STAR
		for (Iterator<?> iterator = selectClauseColumns.iterator(); iterator
				.hasNext();) {
			final QueryResultSpecification queryResultSpec = (QueryResultSpecification) iterator.next();
			if(theColumnToFindTable.equals(((ResultColumn)queryResultSpec).getValueExpr().getName())){
				return lookUpTheFromClause(selectQueryObject, theColumnToFindTable, map);
			}
		}

		return Collections.emptyList();
	}

	private List<String> lookUpTableInDatabase(final TableInDatabase tableInDatabase,
			String columnToFindTable) {
		if(isStar(columnToFindTable)){
			return Collections.singletonList(tableInDatabase.getName());

		}
		EList<?> valueExprColumns = tableInDatabase.getValueExprColumns();
		for (Iterator<?> iterator = valueExprColumns.iterator(); iterator
				.hasNext();) {
			final ValueExpressionColumn column = (ValueExpressionColumn) iterator.next();
			if(column.getName().equals(columnToFindTable)){
				return Collections.singletonList(tableInDatabase.getName());
			}

		}
		//ambiguity , potential table, add a ? mark at the end
		return Collections.singletonList(tableInDatabase.getName()+"?");
	}

	private List<String> lookUpTheFromClause(QuerySelect selectQueryObject, String theColumnToFindTable, Map<String, List<String>> map) {
		/*
		 * if we have the MATCHING,
		 * then loop through every TABLE to see if it contains "theColumnToFindTable";
		 * at the end, we will have this element in the map:
		 * columnName(alias) <-> a list of table names
		 */
		List<String> list = new ArrayList<String>();
		final EList<?> fromClauseList = selectQueryObject.getFromClause();
		for (Iterator<?> fromClauseIterator = fromClauseList.iterator(); fromClauseIterator
				.hasNext();) {
			TableReference tableRef = (TableReference) fromClauseIterator.next();
			list.addAll(findTableNameInTableInDatabaseFrom(tableRef, theColumnToFindTable));//TableInDatabase
			list.addAll(findTableNameInTableJoinedFrom(tableRef, theColumnToFindTable, map));//TableJoined
			list.addAll(findTableNameInQuerySelectFrom(tableRef, theColumnToFindTable, map));//QuerySelect

		}
		return list;

	}

	private List<String> lookUpValueExpressionColumn(
			ValueExpressionColumn valueExpressionColumn) {
		TableExpression tableExpr = valueExpressionColumn.getTableExpr();
		List<String> list = new ArrayList<String>();
		list.addAll(findTableNameInTableInDatabaseFrom(tableExpr, valueExpressionColumn.getName()));//TableInDatabase
		list.addAll(findTableNameInQuerySelectFrom(tableExpr, valueExpressionColumn.getName()));//QuerySelect
		return list;
	}

	@Override
	public List<Map<String, String>> retrieveConstantAndVariable(
			String selectQuery) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map <String, List<String>> retrieveIntoClause(String selectQuery) throws SQLParserException, SQLParserInternalException {

		QuerySelect selectQueryObject = getFirstQuerySelect(selectQuery);
		EList<?> selectClauseColumns = selectQueryObject.getSelectClause();
		if(selectClauseColumns.isEmpty()){
			final Map<String, List<String>> map = new Hashtable<String, List<String>>();

			Map<String, List<String>> singletonMap = Collections.singletonMap(STAR, lookUpQuerySelect(selectQueryObject, STAR, map));
			if(!map.isEmpty() && map.containsKey(STAR)){
				List<String> list = map.get(STAR);
				list.addAll(singletonMap.get(STAR));
				map.put(STAR, list);
				return map;
			}
			//map is not empty or map does not contain STAR
			map.putAll(singletonMap);
			return map;
		}

		/*
		 * if we have columns in the SELECT clause,
		 * then loop through every COLUMN to find the tables it belongs to;
		 * at the end, we will have this element in the map:
		 * columnName(alias) <-> a list of table names
		 */
		final Map<String, List<String>> map = new Hashtable<String, List<String>>(selectClauseColumns.size());
		for (Iterator<?> iterator = selectClauseColumns.iterator(); iterator
				.hasNext();) {
			QueryResultSpecification oneSelectClauseColumn = (QueryResultSpecification) iterator.next();

			try{
				final ResultColumn oneResultColumn = (ResultColumn)oneSelectClauseColumn;
				final QueryValueExpression valueExpr = oneResultColumn.getValueExpr();
				final String alias = (oneResultColumn.getName() == null)?"":"(" +oneResultColumn.getName() +")";

				//TODO: i see async opportunity ...
				map.putAll(findColumnTableMapInValueExpressionColumnFrom(valueExpr, alias));//ValueExpressionColumn
				map.putAll(findColumnTableMapInValueExpressionCaseSimpleFrom(valueExpr, alias));//ValueExpressionCaseSimple
				map.putAll(findColumnTableMapInValueExpressionFunctionFrom(valueExpr, alias));//ValueExpressionFunction
				map.putAll(findColumnTableMapInValueExpressionCastFrom(valueExpr, alias));//ValueExpressionCast

			}catch(ClassCastException e){
				e.printStackTrace();
			}catch(NullPointerException e){
				//table expression of the QueryResultSpecification is NULL
				List<String> findTableNameIn = findTableNameIn(selectQueryObject, STAR);
				List<String> temp = new ArrayList<String>(findTableNameIn.size());
				for (String string : findTableNameIn) {
					temp.add(string +"?");
				}
				map.put(((ResultColumn)oneSelectClauseColumn).getValueExpr().getName()
						+ ((oneSelectClauseColumn.getName() == null)?"":"(" + oneSelectClauseColumn.getName() + ")"),
						temp);

			}
		}

		return map;

	}


}