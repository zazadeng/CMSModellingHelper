package com.wcb.cms.modelmaker.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;

import org.eclipse.datatools.sqltools.parsers.sql.SQLParserException;
import org.eclipse.datatools.sqltools.parsers.sql.SQLParserInternalException;
import org.junit.Test;

public class SelectQueryReaderDTPPlugInImplTest {


	private final SelectQueryReaderDTPPlugInImpl selectQueryReaderDTPPlugInImpl =new SelectQueryReaderDTPPlugInImpl();


	private void selectCASE() throws SQLParserException, SQLParserInternalException {
		/*
		 * Select: CASE column
		 * from:
		 */
		String selectQuery = "SELECT "+
				"       CASE PDA.AEDMNTHLYERNGSCD				" +
				"              WHEN 'NET'						" +
				"              THEN AECR.NEMTHAMT				" +
				"              WHEN 'GROSS'						" +
				"              THEN AECR.GEADJSTDMTHLYAMT		" +
				"       END MONTHLYEARNINGS                		" +
				"FROM   WORKERCMPSNAWARD WCA\r\n" +
				"       INNER JOIN PERMDISABAWARD PDA\r\n" +
				"       ON     PDA.WORKERCMPSNAWARDID = WCA.WORKERCMPSNAWARDID" +
				"		INNER JOIN AEDCALCRESULT AECR\r\n" +
				"       ON     AECR.DECISIONID = PDA.DECISIONID";
		Map<String, List<String>> retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		assertEquals(1, retrieveIntoClause.size());
		List<String> list = retrieveIntoClause.get("NEMTHAMT(MONTHLYEARNINGS)");
		assertEquals(1, list.size());
		for (String table : list) {
			assertTrue("We should NOT see this table " + table + " in the list",
					table.equals("AEDCALCRESULT"));
		}

	}

	private void selectCOLUMN() throws SQLParserException,
	SQLParserInternalException {
		/*
		 * Select:	implicit referencing "claimcycleid"
		 * From:	Query Select
		 */
		String selectQuery = "select claimcycleid from (select claimcycleid from claimcycle) cc";
		Map<String, List<String>> retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		List<String> list = retrieveIntoClause.get("claimcycleid".toUpperCase());
		assertEquals(1, list.size());
		for (String table : list) {
			assertTrue("We should NOT see this table " + table + " in the list",
					table.equals("ClaimCycle".toUpperCase()));
		}

		/*
		 * Select:	referencing "cc.claimcycleid"
		 * From:	Query Select
		 */
		selectQuery = "select cc.claimcycleid from (select claimcycleid from claimcycle) cc";
		retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		list = retrieveIntoClause.get("claimcycleid".toUpperCase());
		assertEquals(1, list.size());
		for (String table : list) {
			assertTrue("We should NOT see this table " + table + " in the list",
					table.equals("ClaimCycle".toUpperCase()));
		}

		/*
		 * Select: 	a column from a nested Select query
		 * from: 	Table joined (nested with Select query)
		 */
		selectQuery = "select wc.claimid from claimcycle cc inner join (select * from wcoclaim, caseheader ch) wc on cc.claimid = wc.claimid";
		retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		assertEquals(1, retrieveIntoClause.size());
		list = retrieveIntoClause.get("claimid".toUpperCase());
		assertEquals(2, list.size());
		for (String table : list) {
			assertTrue("We should NOT see this table " + table + " in the list",
					table.equals("wcoclaim?".toUpperCase())||table.equals("caseheader?".toUpperCase()));
		}

		/*
		 * Select: 	one column
		 * from: 	Table in database
		 * 
		 */
		selectQuery = "select cc.claimID from ClaimCycle cc";
		retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		assertEquals(1, retrieveIntoClause.size());
		list = retrieveIntoClause.get("claimid".toUpperCase());
		assertEquals(1, list.size());
		for (String table : list) {
			assertTrue("We should NOT see this table " + table + " in the list",
					table.equals("ClaimCycle".toUpperCase()));
		}

		/*
		 * Select: 	one column
		 * from: 	Table in database
		 * 
		 * no alias, can NOT assume which table has this column
		 */
		selectQuery = "select claimID from ClaimCycle, wcoclaim";
		retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		assertEquals(1, retrieveIntoClause.size());
		list = retrieveIntoClause.get("claimid".toUpperCase());
		assertEquals(2, list.size());
		for (String table : list) {
			assertTrue("We should NOT see this table " + table + " in the list",
					table.equals("ClaimCycle?".toUpperCase()) || table.equals("wcoclaim?".toUpperCase()));
		}

		/*
		 * Select: 	two columns
		 * from: 	Table in database
		 * 
		 * no alias, can NOT assume which table has this column
		 */
		selectQuery = "select claimcycleid, claimID from ClaimCycle, wcoclaim";
		retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		assertEquals(2, retrieveIntoClause.size());
		list = retrieveIntoClause.get("claimcycleid".toUpperCase());
		assertEquals(2, list.size());
		for (String table : list) {
			assertTrue("We should NOT see this table " + table + " in the list",
					table.equals("ClaimCycle?".toUpperCase()) || table.equals("wcoclaim?".toUpperCase()));
		}
		list = retrieveIntoClause.get("claimid".toUpperCase());
		assertEquals(2, list.size());
		for (String table : list) {
			assertTrue("We should NOT see this table " + table + " in the list",
					table.equals("ClaimCycle?".toUpperCase()) || table.equals("wcoclaim?".toUpperCase()));
		}



	}

	private void selectFUNCTION() throws SQLParserException,
	SQLParserInternalException {
		/*
		 * 
		 */
		String selectQuery = "select COUNT(*) from wcoclaim";
		Map<String, List<String>> retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		assertEquals(1, retrieveIntoClause.size());
		List<String> list = retrieveIntoClause.get("COUNT");
		assertEquals(1, list.size());
		for (String table : list) {
			assertTrue("We should NOT see this table " + table + " in the list",
					table.equals("FUNCTION:COUNT"));
		}

		/*
		 * Select: FUNCTION column
		 * from:
		 */
		selectQuery = "SELECT " +
				"       DATE(PDCAL1.BUSINESSCALCDTM)    APPROVAL_DATE   \r\n" +
				"FROM   WORKERCMPSNAWARD WCA 				\r\n" +
				"INNER JOIN PDACALCULATION PDCAL1 			\r\n" +
				"       ON     PDCAL1.WORKERCMPSNAWARDID = WCA.WORKERCMPSNAWARDID";
		retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		assertEquals(1, retrieveIntoClause.size());
		list = retrieveIntoClause.get("DATE(APPROVAL_DATE)");
		assertEquals(1, list.size());
		for (String table : list) {
			assertTrue("We should NOT see this table " + table + " in the list",
					table.equals("FUNCTION:DATE"));
		}


		/*
		 * Select: FUNCTION column with alias
		 * from:
		 */
		selectQuery ="SELECT " +
				"       DATE(FTD.BUSADDEDDTM)  \r\n" +
				"FROM   WORKERCMPSNAWARD WCA\r\n"  +
				"LEFT OUTER JOIN FINTRANSDET FTD\r\n" +
				"       ON     FTD.WORKERCMPSNAWARDID  = WCA.WORKERCMPSNAWARDID";
		retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		assertEquals(1, retrieveIntoClause.size());
		list = retrieveIntoClause.get("BUSADDEDDTM_DATE");
		assertEquals(1, list.size());
		for (String table : list) {
			assertTrue("We should NOT see this table " + table + " in the list",
					table.equals("FUNCTION:DATE"));
		}

		/*
		 * Select: two columns, the second one is acting as an empty variable (place holder), maybe for later update of the variable
		 * 
		 */
		selectQuery = "select claimcycleid, CAST ( NULL AS INTEGER) MONTHLYEARNINGS from ClaimCycle";
		retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		assertEquals(2, retrieveIntoClause.size());
		list = retrieveIntoClause.get("claimcycleid".toUpperCase());
		assertEquals(1, list.size());
		for (String table : list) {
			assertTrue("We should NOT see this table " + table + " in the list",
					table.equals("ClaimCycle".toUpperCase()));
		}
		list = retrieveIntoClause.get("INTEGER(MONTHLYEARNINGS)");
		assertEquals(1, list.size());
		for (String table : list) {
			assertTrue("We should NOT see this table " + table + " in the list",
					table.equals("FUNCTION:CAST"));
		}
	}

	private void selectSTAR() throws SQLParserException,
	SQLParserInternalException {
		String selectQuery = "";
		/*
		 * Select: 	*
		 * From:  	Table expression
		 */
		selectQuery = "select * from ClaimCycle, wcoclaim";

		Map<String, List<String>> retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		assertEquals(1, retrieveIntoClause.size());
		String value = "ClaimCycle".toUpperCase();//DONE BY the parser
		List<String> list = retrieveIntoClause.get("*");
		assertEquals(2, list.size());
		for (String table : list) {
			assertTrue("We should NOT see this table " + table + " in the list",
					table.equals(value)|| table.equals("wcoclaim".toUpperCase()));
		}

		/*
		 * Select: 	*
		 * from: 	Table joined
		 */
		selectQuery = "select * from claimcycle cc inner join wcoclaim wc on cc.claimid = wc.claimid";
		retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		assertEquals(1, retrieveIntoClause.size());
		list = retrieveIntoClause.get("*");
		assertEquals(2, list.size());
		for (String table : list) {
			assertTrue("We should NOT see this table " + table + " in the list",
					table.equals("ClaimCycle".toUpperCase())|| table.equals("wcoclaim".toUpperCase()));
		}


		/*
		 * Select:	*
		 * From:	Query Select
		 */
		selectQuery = "select * from (select * from claimcycle) cc";
		retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		assertEquals(1, retrieveIntoClause.size());
		list = retrieveIntoClause.get("*");
		assertEquals(1, list.size());
		for (String table : list) {
			assertTrue("We should NOT see this table " + table + " in the list",
					table.equals("ClaimCycle".toUpperCase()));
		}

		/*
		 * Select:	*
		 * From:	Query Select
		 */
		selectQuery = "select * from (select claimcycleid from claimcycle,caseheader) cc";
		retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		list = retrieveIntoClause.get("*");
		assertEquals(0, list.size());
		list = retrieveIntoClause.get("claimcycleid".toUpperCase());
		assertEquals(2, list.size());
		for (String table : list) {
			assertTrue("We should NOT see this table " + table + " in the list",
					table.equals("ClaimCycle?".toUpperCase()) || table.equals("caseheader?".toUpperCase()));
		}



		/*
		 * Select: 	*
		 * from: 	Table joined (nested with Select query)
		 */
		selectQuery = "select * from claimcycle cc inner join (select * from wcoclaim) wc on cc.claimid = wc.claimid";
		retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		assertEquals(1, retrieveIntoClause.size());
		list = retrieveIntoClause.get("*");
		assertEquals(2, list.size());
		for (String table : list) {
			assertTrue("We should NOT see this table " + table + " in the list",
					table.equals("ClaimCycle".toUpperCase())|| table.equals("wcoclaim".toUpperCase()));
		}

	}

	@Test
	public void testGetTableColumnsSet() {

		try {
			selectSTAR();
			selectFUNCTION();
			selectCOLUMN();
			selectCASE();

		} catch (SQLParserException | SQLParserInternalException e) {
			fail("Ooopss... should not happen!");
			e.printStackTrace();
		}


	}




}
