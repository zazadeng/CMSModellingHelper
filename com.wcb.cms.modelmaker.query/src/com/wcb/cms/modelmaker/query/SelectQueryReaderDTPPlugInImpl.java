package com.wcb.cms.modelmaker.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.datatools.modelbase.sql.datatypes.PredefinedDataType;
import org.eclipse.datatools.modelbase.sql.query.Predicate;
import org.eclipse.datatools.modelbase.sql.query.QueryCombined;
import org.eclipse.datatools.modelbase.sql.query.QueryExpressionBody;
import org.eclipse.datatools.modelbase.sql.query.QueryResultSpecification;
import org.eclipse.datatools.modelbase.sql.query.QuerySelect;
import org.eclipse.datatools.modelbase.sql.query.QuerySelectStatement;
import org.eclipse.datatools.modelbase.sql.query.QueryValueExpression;
import org.eclipse.datatools.modelbase.sql.query.ResultColumn;
import org.eclipse.datatools.modelbase.sql.query.ResultTableAllColumns;
import org.eclipse.datatools.modelbase.sql.query.TableCorrelation;
import org.eclipse.datatools.modelbase.sql.query.TableExpression;
import org.eclipse.datatools.modelbase.sql.query.TableInDatabase;
import org.eclipse.datatools.modelbase.sql.query.TableJoined;
import org.eclipse.datatools.modelbase.sql.query.TableReference;
import org.eclipse.datatools.modelbase.sql.query.ValueExpressionCaseSimple;
import org.eclipse.datatools.modelbase.sql.query.ValueExpressionCaseSimpleContent;
import org.eclipse.datatools.modelbase.sql.query.ValueExpressionCast;
import org.eclipse.datatools.modelbase.sql.query.ValueExpressionColumn;
import org.eclipse.datatools.modelbase.sql.query.ValueExpressionFunction;
import org.eclipse.datatools.modelbase.sql.query.ValueExpressionSimple;
import org.eclipse.datatools.modelbase.sql.query.ValueExpressionVariable;
import org.eclipse.datatools.sqltools.parsers.sql.SQLParserException;
import org.eclipse.datatools.sqltools.parsers.sql.SQLParserInternalException;
import org.eclipse.datatools.sqltools.parsers.sql.query.SQLQueryParserManager;
import org.eclipse.datatools.sqltools.parsers.sql.query.SQLQueryParserManagerProvider;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

import com.wcb.cms.modelmaker.api.CMSEntityEntry;
import com.wcb.cms.modelmaker.api.ErrorMessages;
import com.wcb.cms.modelmaker.api.SelectQueryReader;

/**
 * Implementation of SelectQueryReader
 */
public final class SelectQueryReaderDTPPlugInImpl implements SelectQueryReader {

	private final static SQLQueryParserManager parserManager =
			SQLQueryParserManagerProvider.getInstance().getParserManager(null, null);
	private static final String STAR = "*";
	/**
	 * NOTE: This constructor is needed to make use of Blueprint.
	 */
	public SelectQueryReaderDTPPlugInImpl(){
		//no instance variables ... yeah!
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

	private Map<String, Map<String, List<String>>> findPredicateInParentOfValueExpressionSimpleFrom(
			Object object) {
		try{
			ValueExpressionSimple valExp = (ValueExpressionSimple) object;

			if(valExp.getValue().matches("'.*'")){
				for (Iterator<?> iterator = valExp.eContainer().eAllContents(); iterator
						.hasNext();) {
					try{
						QueryValueExpression queryValueExpression = (QueryValueExpression) iterator.next();
						Map<String, List<String>> value = new Hashtable<>(1);
						value.put(queryValueExpression.getName(), findTableNameInValueExpressionColumnFrom(queryValueExpression));
						if(value.get(queryValueExpression.getName()).size()>0){
							return Collections.singletonMap(((Predicate)valExp.eContainer()).getSourceInfo().getSourceSnippet(), value);
						}
					}catch(ClassCastException ee){
						//continue;
					}catch(NullPointerException ee){
						//continue;
					}
				}
			}


		}catch(ClassCastException e){
			//it is ok
		}
		return Collections.emptyMap();
	}

	private Map<String, Map<String, List<String>>> findPredicateInParentOfValueExpressionVariableFrom(
			Object next) {
		try{

			ValueExpressionVariable valExpVar = (ValueExpressionVariable)next;
			//PredicateBasic parent = (PredicateBasic)valExpVar.eContainer();
			for (Iterator<?> iterator = valExpVar.eContainer().eAllContents(); iterator
					.hasNext();) {
				try{
					QueryValueExpression queryValueExpression = (QueryValueExpression) iterator.next();
					Map<String, List<String>> value = new Hashtable<>(1);
					value.put(queryValueExpression.getName(), findTableNameInValueExpressionColumnFrom(queryValueExpression));
					if(value.get(queryValueExpression.getName()).size()>0){
						return Collections.singletonMap(((Predicate)valExpVar.eContainer()).getSourceInfo().getSourceSnippet(), value);
					}
				}catch(ClassCastException ee){
					//continue;
				}catch(NullPointerException ee){
					//continue;
				}
			}

			//expList.add(theSql);

		}catch(ClassCastException e){
			//it is ok
		}
		return Collections.emptyMap();
	}



	private List<String> findTableNameInQuerySelectFrom(
			TableExpression tableExpr, String theColumnToFindTable) {
		return findTableNameInQuerySelectFrom(tableExpr, theColumnToFindTable, null);
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

	/*private List<String> findTableNameInTableInDatabaseFrom(
			TableExpression tableExpr) {
		try{
			final TableInDatabase tableInDatabase = (TableInDatabase)tableExpr;
			return lookUpTableInDatabase(tableInDatabase);
		}catch(ClassCastException e){
			return Collections.emptyList();
		}

	}*/

	private List<String> findTableNameInTableInDatabaseFrom(TableReference tableRef) {
		try{
			final TableInDatabase tableInDB = (TableInDatabase)tableRef;
			return lookUpTableInDatabase(tableInDB);
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
			list.addAll(findTableNameInTableInDatabaseFrom(tableRefRight));//TableInDatabase
			list.addAll(findTableNameInQuerySelectFrom(tableRefRight, columnToFindTable, map));//QuerySelect

			/*
			 * LEFT
			 */
			TableReference tableRefLeft = tableJoined.getTableRefLeft();
			list.addAll(findTableNameInTableInDatabaseFrom(tableRefLeft));//TableInDatabase
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
	}

	private QuerySelect getFirstQuerySelect(String sql)
			throws SQLParserException, SQLParserInternalException {


		QueryExpressionBody queryExprBody = getSelectQueryBody(sql);

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


	private QueryExpressionBody getSelectQueryBody(String selectQuery)
			throws SQLParserException, SQLParserInternalException {
		QuerySelectStatement resultObject = (QuerySelectStatement)parserManager
				.parseQuery(selectQuery)
				.getQueryStatement();

		return resultObject.getQueryExpr().getQuery();
	}

	private boolean isStar(String columnName) {
		return columnName.equals(STAR);
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

	private List<String> lookUpTableInDatabase(TableInDatabase tableInDatabase) {
		TableCorrelation tableCo = tableInDatabase.getTableCorrelation();
		if(tableCo == null) {
			return Collections.singletonList(tableInDatabase.getName());
		}
		return Collections.singletonList(tableInDatabase.getName()+"("+tableCo.getName()+")");
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
			list.addAll(findTableNameInTableInDatabaseFrom(tableRef));//TableInDatabase
			list.addAll(findTableNameInTableJoinedFrom(tableRef, theColumnToFindTable, map));//TableJoined
			list.addAll(findTableNameInQuerySelectFrom(tableRef, theColumnToFindTable, map));//QuerySelect
			try{
				QuerySelect qs = (QuerySelect)tableRef.eContainer();
				TableCorrelation coRel = qs.getTableCorrelation();
				if(coRel != null){
					//reset
					for (int i = 0; i< list.size(); i++) {
						list.set(i, list.get(i).replaceFirst("\\(.+\\)", "") + "("+ coRel.getName()+")");
					}
				}
			}catch(ClassCastException e){
				//continues
			}

		}
		return list;

	}

	private List<String> lookUpValueExpressionColumn(
			ValueExpressionColumn valueExpressionColumn) {
		List<String> list = new ArrayList<String>();
		try{
			TableExpression tableExpr = valueExpressionColumn.getTableExpr();
			list.addAll(findTableNameInTableInDatabaseFrom(tableExpr));//TableInDatabase
			list.addAll(findTableNameInQuerySelectFrom(tableExpr, valueExpressionColumn.getName()));//QuerySelect
		}catch(NullPointerException e){
			//table expression of this valueExpr is NULL, something like this claimcycle(instead of cc.claimcycle) in the select clause
			QuerySelect selectQueryObject = null;
			EObject temp = valueExpressionColumn;
			synchronized (temp) {
				//find Query select
				while(true){
					//note here that we won't have a infinitely loop here
					//because temp will be null eventually, i.e., the given sql is not a select query
					//which is not possible to reach here
					//because it will fail at the beginning while castint QuerySelect
					try{
						selectQueryObject = (QuerySelect)temp.eContainer();
						break;
					}catch(ClassCastException ee){
						temp = temp.eContainer();
					}
				}
			}
			//look up all the tables in the "from clause" by setting STAR
			//and stuff all the possible tables as this column's potential tables
			Map<String, List<String>> newMap = new HashMap<>();
			lookUpQuerySelect(selectQueryObject, STAR, newMap );
			return newMap.get(valueExpressionColumn.getName());
		}
		return list;
	}

	@Override
	public final List<CMSEntityEntry> retrieveConstantAndVariable(
			String selectQuery) throws SQLParserException, SQLParserInternalException {

		final Map<String, Map<String, List<String>>> expMap = new Hashtable<String, Map<String, List<String>>>();
		QueryExpressionBody queryExprBody = getSelectQueryBody(selectQuery);
		for (Iterator<?> iterator = queryExprBody.eAllContents(); iterator.hasNext();) {
			final Object next = iterator.next();
			expMap.putAll(findPredicateInParentOfValueExpressionSimpleFrom(next));//ValueExpressionSimple
			expMap.putAll(findPredicateInParentOfValueExpressionVariableFrom(next));//ValueExpressionVariable

		}
		List<CMSEntityEntry> list = new ArrayList<>();
		for (String sqlElement : expMap.keySet()) {
			list.add(new CMSEntityEntry(expMap.get(sqlElement).keySet().iterator().next())
			.setSqlElement(sqlElement)
			.addToPotentialTableList(Collections.singletonList(expMap.get(sqlElement).values().iterator().next().get(0))));
		}
		return list;
	}

	@Override
	public final List<CMSEntityEntry> retrieveIntoClause(String selectQuery) throws SQLParserException, SQLParserInternalException, IOException {
		QuerySelect selectQueryObject = getFirstQuerySelect(selectQuery);
		EList<?> selectClauseColumns = selectQueryObject.getSelectClause();

		if(selectClauseColumns.isEmpty()){
			final Map<String, List<String>> map = new Hashtable<>();
			Map<String, List<String>> mapForStar = Collections.singletonMap(STAR,
					lookUpQuerySelect(selectQueryObject, STAR, map));

			map.putAll(mapForStar);
			List<CMSEntityEntry> list  = new ArrayList<>();
			for (String column : map.keySet()) {
				list.add(new CMSEntityEntry(column)
				.setSqlElement(STAR)
				.addToPotentialTableList(map.get(column)));

			}
			return list;
		}
		/*
		 * if we have columns in the SELECT clause,
		 * then loop through every COLUMN to find the tables it belongs to;
		 * at the end, we will have this element in the map:
		 * columnName(alias) <-> a list of table names
		 */
		List<CMSEntityEntry> list  = new ArrayList<>();
		for (Iterator<?> iterator = selectClauseColumns.iterator(); iterator
				.hasNext();) {
			QueryResultSpecification oneSelectClauseColumn = (QueryResultSpecification) iterator.next();
			//we only will find ONE, so we set the size to ONE
			final Map<String, List<String>> map = new Hashtable<>(1);

			try{
				final ResultColumn oneResultColumn = (ResultColumn)oneSelectClauseColumn;
				final QueryValueExpression valueExpr = oneResultColumn.getValueExpr();
				final String alias = (oneResultColumn.getName() == null)?"":"(" +oneResultColumn.getName() +")";

				//TODO: concurrency opportunity. make a bunch of futures(callables)
				map.putAll(findColumnTableMapInValueExpressionColumnFrom(valueExpr, alias));//ValueExpressionColumn
				map.putAll(findColumnTableMapInValueExpressionCaseSimpleFrom(valueExpr, alias));//ValueExpressionCaseSimple
				map.putAll(findColumnTableMapInValueExpressionFunctionFrom(valueExpr, alias));//ValueExpressionFunction
				map.putAll(findColumnTableMapInValueExpressionCastFrom(valueExpr, alias));//ValueExpressionCast

			}catch(ClassCastException e){
				final ResultTableAllColumns tableAllColumn = (ResultTableAllColumns)oneSelectClauseColumn;
				TableExpression tableExpr = tableAllColumn.getTableExpr();
				if(tableAllColumn.getName().equals(tableExpr.getName())){
					//"select kk.* from ClaimCycle cc" is parsed ... which shouldn't ...
					throw new IOException(ErrorMessages.Error4(tableExpr.getName()));
				}
				//NOTE: "select cc.* alias from ClaimCycle cc" is an invalid query...
				//final String alias = (tableAllColumn.getName() == null)?"":"(" +tableAllColumn.getName() +")";
				List<String> values = new ArrayList<>();
				values.addAll(findTableNameInTableInDatabaseFrom(tableExpr));//TableInDatabase
				values.addAll(findTableNameInQuerySelectFrom(tableExpr, STAR));//QuerySelect
				map.put(STAR, values);
			}

			for (String column : map.keySet()) {
				list.add(new CMSEntityEntry(column)
				.setSqlElement(oneSelectClauseColumn.getSourceInfo().getSourceSnippet())
				.addToPotentialTableList(map.get(column)));
			}
		}

		return list;

	}


}