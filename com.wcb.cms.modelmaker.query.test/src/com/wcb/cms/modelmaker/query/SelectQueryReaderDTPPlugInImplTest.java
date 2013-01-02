package com.wcb.cms.modelmaker.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.eclipse.datatools.sqltools.parsers.sql.SQLParserException;
import org.eclipse.datatools.sqltools.parsers.sql.SQLParserInternalException;
import org.junit.Test;

import com.wcb.cms.modelmaker.api.CMSEntityEntry;

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
		List<CMSEntityEntry> retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		assertEquals(1, retrieveIntoClause.size());
		for (CMSEntityEntry cmsEntityEntry : retrieveIntoClause) {
			assertEquals("CASE PDA.AEDMNTHLYERNGSCD				" +
					"              WHEN 'NET'						" +
					"              THEN AECR.NEMTHAMT				" +
					"              WHEN 'GROSS'						" +
					"              THEN AECR.GEADJSTDMTHLYAMT		" +
					"       END MONTHLYEARNINGS" , cmsEntityEntry.getSqlElement());
			assertEquals("NEMTHAMT", cmsEntityEntry.getColumn());
			assertEquals("MONTHLYEARNINGS", cmsEntityEntry.getColumnAlias());
			assertEquals("AEDCALCRESULT", cmsEntityEntry.getTable());

		}
		/*List<String> list = retrieveIntoClause.get("NEMTHAMT(MONTHLYEARNINGS)");
		assertEquals(1, list.size());
		for (String table : list) {
			assertTrue("We should NOT see this table " + table + " in the list",
					table.equals("AEDCALCRESULT"));
		}*/

	}

	private void selectCOLUMN() throws SQLParserException,
	SQLParserInternalException {
		/*
		 * Select:	implicit referencing "claimcycleid"
		 * From:	Query Select
		 */
		String selectQuery = "select claimcycleid from (select claimcycleid from claimcycle) cc";
		List<CMSEntityEntry> retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		assertEquals(1, retrieveIntoClause.size());
		for (CMSEntityEntry cmsEntityEntry : retrieveIntoClause) {
			assertEquals("claimcycleid", cmsEntityEntry.getSqlElement());
			assertEquals("claimcycleid".toUpperCase(), cmsEntityEntry.getColumn());
			assertEquals("ClaimCycle".toUpperCase(), cmsEntityEntry.getTable());

		}
		/*List<String> list = retrieveIntoClause.get("claimcycleid".toUpperCase());
		assertEquals(1, list.size());
		for (String table : list) {
			assertTrue("We should NOT see this table " + table + " in the list",
					table.equals("ClaimCycle".toUpperCase()));
		}*/

		/*
		 * Select:	referencing "cc.claimcycleid"
		 * From:	Query Select
		 */
		selectQuery = "select cc.claimcycleid from (select claimcycleid from claimcycle) cc";
		retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		assertEquals(1, retrieveIntoClause.size());
		for (CMSEntityEntry cmsEntityEntry : retrieveIntoClause) {
			assertEquals("cc.claimcycleid", cmsEntityEntry.getSqlElement());
			assertEquals("claimcycleid".toUpperCase(), cmsEntityEntry.getColumn());
			assertEquals("ClaimCycle".toUpperCase(), cmsEntityEntry.getTable());

		}

		/*list = retrieveIntoClause.get("claimcycleid".toUpperCase());
		assertEquals(1, list.size());
		for (String table : list) {
			assertTrue("We should NOT see this table " + table + " in the list",
					table.equals("ClaimCycle".toUpperCase()));
		}*/

		/*
		 * Select: 	a column from a nested Select query
		 * from: 	Table joined (nested with Select query)
		 */
		selectQuery = "select wc.claimid from claimcycle cc inner join (select * from wcoclaim, caseheader ch) wc on cc.claimid = wc.claimid";
		retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		assertEquals(1, retrieveIntoClause.size());
		for (CMSEntityEntry cmsEntityEntry : retrieveIntoClause) {
			assertEquals("claimid".toUpperCase(), cmsEntityEntry.getColumn());
			assertEquals("", cmsEntityEntry.getTable());
			assertEquals(2, cmsEntityEntry.getPotentialTableList().size());
			for (String table : cmsEntityEntry.getPotentialTableList()) {
				assertTrue("We should NOT see this table " + table + " in the list",
						table.equals("wcoclaim".toUpperCase())||table.equals("caseheader".toUpperCase()));
			}
		}
		/*list = retrieveIntoClause.get("claimid".toUpperCase());
		assertEquals(2, list.size());
		for (String table : list) {
			assertTrue("We should NOT see this table " + table + " in the list",
					table.equals("wcoclaim?".toUpperCase())||table.equals("caseheader?".toUpperCase()));
		}*/

		/*
		 * Select: 	one column
		 * from: 	Table in database
		 * 
		 */
		selectQuery = "select cc.claimID from ClaimCycle cc";
		retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		assertEquals(1, retrieveIntoClause.size());
		for (CMSEntityEntry cmsEntityEntry : retrieveIntoClause) {
			assertEquals("claimid".toUpperCase(), cmsEntityEntry.getColumn());
			assertEquals("ClaimCycle".toUpperCase(), cmsEntityEntry.getTable());

		}

		/*list = retrieveIntoClause.get("claimid".toUpperCase());
		assertEquals(1, list.size());
		for (String table : list) {
			assertTrue("We should NOT see this table " + table + " in the list",
					table.equals("ClaimCycle".toUpperCase()));
		}*/

		/*
		 * Select: 	one column
		 * from: 	Table in database
		 * 
		 * no referencing, can NOT assume which table has this column, but wcoclaim doesn't have claimcycleid, so it is a correct query though
		 */
		selectQuery = "select claimCycleid SSS from ClaimCycle, wcoclaim";
		retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		assertEquals(1, retrieveIntoClause.size());
		for (CMSEntityEntry cmsEntityEntry : retrieveIntoClause) {
			assertEquals("claimCycleid".toUpperCase(), cmsEntityEntry.getColumn());
			assertEquals("", cmsEntityEntry.getTable());
			assertEquals(2, cmsEntityEntry.getPotentialTableList().size());
			for (String table : cmsEntityEntry.getPotentialTableList()) {
				assertTrue("We should NOT see this table " + table + " in the list",
						table.equals("wcoclaim".toUpperCase())||table.equals("ClaimCycle".toUpperCase()));
			}
		}

		/*list = retrieveIntoClause.get("claimid".toUpperCase());
		assertEquals(2, list.size());
		for (String table : list) {
			assertTrue("We should NOT see this table " + table + " in the list",
					table.equals("ClaimCycle?".toUpperCase()) || table.equals("wcoclaim?".toUpperCase()));
		}*/

		/*
		 * Select: 	two columns
		 * from: 	Table in database
		 * 
		 * no referencing, can NOT assume which table has this column
		 */
		selectQuery = "select claimcycleid, claimID from ClaimCycle, wcoclaim";
		retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);

		assertEquals(2, retrieveIntoClause.size());
		for (CMSEntityEntry cmsEntityEntry : retrieveIntoClause) {
			if(cmsEntityEntry.getColumn().equals("claimid".toUpperCase())){
				assertEquals("", cmsEntityEntry.getTable());
				assertEquals(2, cmsEntityEntry.getPotentialTableList().size());
				for (String table : cmsEntityEntry.getPotentialTableList()) {
					assertTrue("We should NOT see this table " + table + " in the list",
							table.equals("ClaimCycle".toUpperCase())||table.equals("wcoclaim".toUpperCase()));
				}
			}else if(cmsEntityEntry.getColumn().equals("claimcycleid".toUpperCase())){
				assertEquals("", cmsEntityEntry.getTable());
				assertEquals(2, cmsEntityEntry.getPotentialTableList().size());
				for (String table : cmsEntityEntry.getPotentialTableList()) {
					assertTrue("We should NOT see this table " + table + " in the list",
							table.equals("ClaimCycle".toUpperCase())||table.equals("wcoclaim".toUpperCase()));
				}
			}else{
				fail("Shouldn't have other cases");
			}

		}

		/*assertEquals(2, retrieveIntoClause.size());
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
		 */


	}

	private void selectFUNCTION() throws SQLParserException,
	SQLParserInternalException {
		/*
		 * 
		 */
		String selectQuery = "select COUNT(*) from wcoclaim";
		List<CMSEntityEntry> retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		assertEquals(1, retrieveIntoClause.size());
		for (CMSEntityEntry cmsEntityEntry : retrieveIntoClause) {
			assertEquals("COUNT", cmsEntityEntry.getColumn());
			assertEquals("", cmsEntityEntry.getColumnAlias());
			assertEquals("FUNCTION:COUNT", cmsEntityEntry.getTable());
			assertEquals(1, cmsEntityEntry.getPotentialTableList().size());
			for (String table : cmsEntityEntry.getPotentialTableList()) {
				assertTrue("We should NOT see this table " + table + " in the list",
						table.equals("FUNCTION:COUNT"));
			}
			assertTrue(cmsEntityEntry.isFunction());
		}

		/*assertEquals(1, retrieveIntoClause.size());
		List<String> list = retrieveIntoClause.get("COUNT");
		assertEquals(1, list.size());
		for (String table : list) {
			assertTrue("We should NOT see this table " + table + " in the list",
					table.equals("FUNCTION:COUNT"));
		}*/

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
		for (CMSEntityEntry cmsEntityEntry : retrieveIntoClause) {
			assertEquals("DATE(PDCAL1.BUSINESSCALCDTM)    APPROVAL_DATE", cmsEntityEntry.getSqlElement());
			assertEquals("DATE", cmsEntityEntry.getColumn());
			assertEquals("APPROVAL_DATE", cmsEntityEntry.getColumnAlias());
			assertEquals("FUNCTION:DATE", cmsEntityEntry.getTable());
			assertEquals(1, cmsEntityEntry.getPotentialTableList().size());
			for (String table : cmsEntityEntry.getPotentialTableList()) {
				assertTrue("We should NOT see this table " + table + " in the list",
						table.equals("FUNCTION:DATE"));
			}
			assertTrue(cmsEntityEntry.isFunction());
		}

		/*assertEquals(1, retrieveIntoClause.size());
		list = retrieveIntoClause.get("DATE(APPROVAL_DATE)");
		assertEquals(1, list.size());
		for (String table : list) {
			assertTrue("We should NOT see this table " + table + " in the list",
					table.equals("FUNCTION:DATE"));
		}*/


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
		for (CMSEntityEntry cmsEntityEntry : retrieveIntoClause) {
			assertEquals("DATE(FTD.BUSADDEDDTM)", cmsEntityEntry.getSqlElement());
			assertEquals("BUSADDEDDTM_DATE", cmsEntityEntry.getColumn());
			assertEquals("", cmsEntityEntry.getColumnAlias());
			assertEquals("FUNCTION:DATE", cmsEntityEntry.getTable());
			assertEquals(1, cmsEntityEntry.getPotentialTableList().size());
			for (String table : cmsEntityEntry.getPotentialTableList()) {
				assertTrue("We should NOT see this table " + table + " in the list",
						table.equals("FUNCTION:DATE"));
			}
			assertTrue(cmsEntityEntry.isFunction());
		}
		/*assertEquals(1, retrieveIntoClause.size());
		list = retrieveIntoClause.get("BUSADDEDDTM_DATE");
		assertEquals(1, list.size());
		for (String table : list) {
			assertTrue("We should NOT see this table " + table + " in the list",
					table.equals("FUNCTION:DATE"));
		}*/

		/*
		 * Select: two columns, the second one is acting as an empty variable (place holder),
		 * maybe for later update of the variable
		 * 
		 */
		selectQuery = "select claimcycleid, CAST ( NULL AS INTEGER) MONTHLYEARNINGS from ClaimCycle";
		retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		assertEquals(2, retrieveIntoClause.size());
		for (CMSEntityEntry cmsEntityEntry : retrieveIntoClause) {
			if(cmsEntityEntry.getColumn().equals("claimcycleid".toUpperCase())){
				assertEquals("", cmsEntityEntry.getColumnAlias());
				assertEquals("ClaimCycle".toUpperCase(), cmsEntityEntry.getTable());
				assertEquals(1, cmsEntityEntry.getPotentialTableList().size());
				for (String table : cmsEntityEntry.getPotentialTableList()) {
					assertTrue("We should NOT see this table " + table + " in the list",
							table.equals("claimcycle".toUpperCase()));
				}
				assertTrue(cmsEntityEntry.isFunction() == false);
			}else if(cmsEntityEntry.getColumn().equals("INTEGER")){
				assertEquals("CAST ( NULL AS INTEGER) MONTHLYEARNINGS", cmsEntityEntry.getSqlElement());
				assertEquals("MONTHLYEARNINGS", cmsEntityEntry.getColumnAlias());
				assertEquals("FUNCTION:CAST", cmsEntityEntry.getTable());
				assertEquals(1, cmsEntityEntry.getPotentialTableList().size());
				for (String table : cmsEntityEntry.getPotentialTableList()) {
					assertTrue("We should NOT see this table " + table + " in the list",
							table.equals("FUNCTION:CAST"));
				}
				assertTrue(cmsEntityEntry.isFunction());
			}else{
				fail("We shouldn't see other cases!");
			}
		}


		/*assertEquals(2, retrieveIntoClause.size());
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
		}*/
	}

	private void selectSTAR() throws SQLParserException, SQLParserInternalException {
		String selectQuery = "";
		/*
		 * Select: 	*
		 * From:  	Table expression
		 */
		selectQuery = "select * from ClaimCycle, wcoclaim";

		List<CMSEntityEntry> retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		assertEquals(1, retrieveIntoClause.size());
		for (CMSEntityEntry cmsEntityEntry : retrieveIntoClause) {
			assertEquals("*", cmsEntityEntry.getSqlElement());
			assertEquals("*", cmsEntityEntry.getColumn());
			assertEquals("", cmsEntityEntry.getColumnAlias());
			assertEquals("", cmsEntityEntry.getTable());
			assertEquals(2, cmsEntityEntry.getPotentialTableList().size());
			for (String table : cmsEntityEntry.getPotentialTableList()) {
				assertTrue("We should NOT see this table " + table + " in the list",
						table.equals("ClaimCycle".toUpperCase())|| table.equals("wcoclaim".toUpperCase()));
			}
			assertTrue(cmsEntityEntry.isFunction() == false);
		}

		/*assertEquals(1, retrieveIntoClause.size());
		String value = "ClaimCycle".toUpperCase();//DONE BY the parser
		List<String> list = retrieveIntoClause.get("*");
		assertEquals(2, list.size());
		for (String table : list) {
			assertTrue("We should NOT see this table " + table + " in the list",
					table.equals(value)|| table.equals("wcoclaim".toUpperCase()));
		}*/

		/*
		 * Select: 	*
		 * from: 	Table joined
		 */
		selectQuery = "select * from claimcycle cc inner join wcoclaim wc on cc.claimid = wc.claimid";
		retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		assertEquals(1, retrieveIntoClause.size());
		for (CMSEntityEntry cmsEntityEntry : retrieveIntoClause) {
			assertEquals("*", cmsEntityEntry.getSqlElement());
			assertEquals("*", cmsEntityEntry.getColumn());
			assertEquals("", cmsEntityEntry.getColumnAlias());
			assertEquals("", cmsEntityEntry.getTable());
			assertEquals(2, cmsEntityEntry.getPotentialTableList().size());
			for (String table : cmsEntityEntry.getPotentialTableList()) {
				assertTrue("We should NOT see this table " + table + " in the list",
						table.equals("ClaimCycle".toUpperCase())|| table.equals("wcoclaim".toUpperCase()));
			}
			assertTrue(cmsEntityEntry.isFunction() == false);
		}

		/*list = retrieveIntoClause.get("*");
		assertEquals(2, list.size());
		for (String table : list) {
			assertTrue("We should NOT see this table " + table + " in the list",
					table.equals("ClaimCycle".toUpperCase())|| table.equals("wcoclaim".toUpperCase()));
		}
		 */

		/*
		 * Select:	*
		 * From:	Query Select
		 */
		selectQuery = "select * from (select * from claimcycle) cc";
		retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		assertEquals(1, retrieveIntoClause.size());
		for (CMSEntityEntry cmsEntityEntry : retrieveIntoClause) {
			assertEquals("*", cmsEntityEntry.getSqlElement());
			assertEquals("*", cmsEntityEntry.getColumn());
			assertEquals("", cmsEntityEntry.getColumnAlias());
			assertEquals("claimcycle".toUpperCase(), cmsEntityEntry.getTable());
			assertEquals(1, cmsEntityEntry.getPotentialTableList().size());
			for (String table : cmsEntityEntry.getPotentialTableList()) {
				assertTrue("We should NOT see this table " + table + " in the list",
						table.equals("ClaimCycle".toUpperCase()));}
			assertTrue(cmsEntityEntry.isFunction() == false);
		}

		/*list = retrieveIntoClause.get("*");
		assertEquals(1, list.size());
		for (String table : list) {
			assertTrue("We should NOT see this table " + table + " in the list",
					table.equals("ClaimCycle".toUpperCase()));
		}*/

		/*
		 * Select:	*
		 * From:	Query Select
		 */
		selectQuery = "select * from (select claimcycleid from claimcycle,caseheader) cc, wcoclaim";
		retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		assertEquals(2, retrieveIntoClause.size());
		for (CMSEntityEntry cmsEntityEntry : retrieveIntoClause) {
			if(cmsEntityEntry.getColumn().equals("*")){
				assertEquals("*", cmsEntityEntry.getSqlElement());
				assertEquals("", cmsEntityEntry.getColumnAlias());
				assertEquals("wcoclaim".toUpperCase(), cmsEntityEntry.getTable());
				assertEquals(1, cmsEntityEntry.getPotentialTableList().size());
				assertTrue(cmsEntityEntry.isFunction() == false);
			}else if(cmsEntityEntry.getColumn().equals("claimcycleid".toUpperCase())){
				assertEquals("*", cmsEntityEntry.getSqlElement());
				assertEquals("", cmsEntityEntry.getColumnAlias());
				assertEquals("", cmsEntityEntry.getTable());
				assertEquals(2, cmsEntityEntry.getPotentialTableList().size());
				for (String table : cmsEntityEntry.getPotentialTableList()) {
					assertTrue("We should NOT see this table " + table + " in the list",
							table.equals("ClaimCycle".toUpperCase()) || table.equals("caseheader".toUpperCase()));
				}
				assertTrue(cmsEntityEntry.isFunction() == false);
			}else{
				fail("We shouldn't see other cases!");
			}
		}

		/*list = retrieveIntoClause.get("*");
		assertEquals(0, list.size());
		list = retrieveIntoClause.get("claimcycleid".toUpperCase());
		assertEquals(2, list.size());
		for (String table : list) {
			assertTrue("We should NOT see this table " + table + " in the list",
					table.equals("ClaimCycle?".toUpperCase()) || table.equals("caseheader?".toUpperCase()));
		}*/



		/*
		 * Select: 	*
		 * from: 	Table joined (nested with Select query)
		 */
		selectQuery = "select * from claimcycle cc inner join (select * from wcoclaim) wc on cc.claimid = wc.claimid";
		retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		assertEquals(1, retrieveIntoClause.size());
		for (CMSEntityEntry cmsEntityEntry : retrieveIntoClause) {
			assertEquals("*", cmsEntityEntry.getSqlElement());
			assertEquals("*", cmsEntityEntry.getColumn());
			assertEquals("", cmsEntityEntry.getColumnAlias());
			assertEquals("", cmsEntityEntry.getTable());
			assertEquals(2, cmsEntityEntry.getPotentialTableList().size());
			for (String table : cmsEntityEntry.getPotentialTableList()) {
				assertTrue("We should NOT see this table " + table + " in the list",
						table.equals("ClaimCycle".toUpperCase())|| table.equals("wcoclaim".toUpperCase()));
			}
			assertTrue(cmsEntityEntry.isFunction() == false);
		}


		/*list = retrieveIntoClause.get("*");
		assertEquals(2, list.size());
		for (String table : list) {
			assertTrue("We should NOT see this table " + table + " in the list",
					table.equals("ClaimCycle".toUpperCase())|| table.equals("wcoclaim".toUpperCase()));
		}*/

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

	@Test
	public void testRetrieveConstantAndVariable(){
		String selectQuery = "SELECT WPE.WKRPOTEARNID       \r\n" +
				"FROM   WKRPOTEARN WPE\r\n" +
				"       INNER JOIN WKRPOTMTCALCEARN WPMCE\r\n" +
				"       ON     WPE.WKRPOTEARNID       = WPMCE.WKRPOTEARNID\r\n" +
				"       AND    WPE.CLAIMCYCLEID=:claimCycleID\r\n" +
				"       AND    WPE.WKRPOTENARNTYPECD  IN ('OCA', 'Actual Earnings')\r\n" +
				"       AND    WPE.WKRPOTEARNSTTSCD   = 'Finalized'\r\n" +
				"       AND    WPMCE.EARNAMTTYPECD    = :earnAmtTypeCD\r\n" +
				"       AND    WPE.RECORDSTATUSCODE   = 'RST1'\r\n" +
				"       AND    WPMCE.RECORDSTATUSCODE = 'RST1'\r\n" +
				"       INNER JOIN CLAIMCYCLE CC\r\n" +
				"       ON CC.CLAIMCYCLEID = WPE.CLAIMCYCLEID\r\n" +
				"       AND CC.RECORDSTATUSCODE = 'RST1'\r\n" +
				"       AND YEAR(WPMCE.EARNCOLLECTYR) = :startDt\r\n" +
				"	 INNER JOIN REHABPLAN RP\r\n" +
				"	 ON RP.REHABPLANID = WPE.REHABPLANID\r\n" +
				"	 AND RP.APPROVALDT IS NOT NULL\r\n" +
				"	WHERE NOT EXISTS\r\n" +
				"	(SELECT 1 FROM OCCCLSAVGEARN OCA\r\n" +
				"	 WHERE OCA.WKRPOTEARNID = WPE.WKRPOTEARNID       \r\n" +
				"	 AND OCA.OCCCLSAVGREGIONCD <> :regionCode)";
		try {
			List<CMSEntityEntry> retrieveConstantAndVariable = selectQueryReaderDTPPlugInImpl.retrieveConstantAndVariable(selectQuery);
			//"{WPMCE.RECORDSTATUSCODE = 'RST1'={RECORDSTATUSCODE=[WKRPOTMTCALCEARN]}, CC.RECORDSTATUSCODE = 'RST1'={RECORDSTATUSCODE=[CLAIMCYCLE]}, OCCCLSAVGREGIONCD <> :regionCode={OCCCLSAVGREGIONCD=[OCCCLSAVGEARN]}, WPMCE.EARNAMTTYPECD = :earnAmtTypeCD={EARNAMTTYPECD=[WKRPOTMTCALCEARN]}, WPE.CLAIMCYCLEID = :claimCycleID={CLAIMCYCLEID=[WKRPOTEARN]}, WPE.RECORDSTATUSCODE = 'RST1'={RECORDSTATUSCODE=[WKRPOTEARN]}, WPE.WKRPOTEARNSTTSCD = 'Finalized'={WKRPOTEARNSTTSCD=[WKRPOTEARN]}}";
			assertEquals(9, retrieveConstantAndVariable.size());
			for (CMSEntityEntry cmsEntityEntry : retrieveConstantAndVariable) {
				if(cmsEntityEntry.getSqlElement().equals("WPE.CLAIMCYCLEID=:claimCycleID")){
					assertEquals("WKRPOTEARN", cmsEntityEntry.getTable());
					assertEquals("CLAIMCYCLEID", cmsEntityEntry.getColumn());
				}
				else if(cmsEntityEntry.getSqlElement().equals("WPE.WKRPOTENARNTYPECD  IN ('OCA', 'Actual Earnings')")){
					assertEquals("WKRPOTEARN", cmsEntityEntry.getTable());
					assertEquals("WKRPOTENARNTYPECD", cmsEntityEntry.getColumn());
				}
				else if(cmsEntityEntry.getSqlElement().equals("WPE.WKRPOTEARNSTTSCD   = 'Finalized'")){
					assertEquals("WKRPOTEARN", cmsEntityEntry.getTable());
					assertEquals("WKRPOTEARNSTTSCD", cmsEntityEntry.getColumn());
				}
				else if(cmsEntityEntry.getSqlElement().equals("WPMCE.EARNAMTTYPECD    = :earnAmtTypeCD")){
					assertEquals("WKRPOTMTCALCEARN", cmsEntityEntry.getTable());
					assertEquals("EARNAMTTYPECD", cmsEntityEntry.getColumn());
				}
				else if(cmsEntityEntry.getSqlElement().equals("WPE.RECORDSTATUSCODE   = 'RST1'")){
					assertEquals("WKRPOTEARN", cmsEntityEntry.getTable());
					assertEquals("RECORDSTATUSCODE", cmsEntityEntry.getColumn());
				}
				else if(cmsEntityEntry.getSqlElement().equals("WPMCE.RECORDSTATUSCODE = 'RST1'")){
					assertEquals("WKRPOTMTCALCEARN", cmsEntityEntry.getTable());
					assertEquals("RECORDSTATUSCODE", cmsEntityEntry.getColumn());
				}
				else if(cmsEntityEntry.getSqlElement().equals("CC.RECORDSTATUSCODE = 'RST1'")){
					assertEquals("CLAIMCYCLE", cmsEntityEntry.getTable());
					assertEquals("RECORDSTATUSCODE", cmsEntityEntry.getColumn());
				}
				else if(cmsEntityEntry.getSqlElement().equals("YEAR(WPMCE.EARNCOLLECTYR) = :startDt")){
					assertEquals("WKRPOTMTCALCEARN", cmsEntityEntry.getTable());
					assertEquals("EARNCOLLECTYR", cmsEntityEntry.getColumn());
				}
				else if(cmsEntityEntry.getSqlElement().equals("OCA.OCCCLSAVGREGIONCD <> :regionCode")){
					assertEquals("OCCCLSAVGEARN", cmsEntityEntry.getTable());
					assertEquals("OCCCLSAVGREGIONCD", cmsEntityEntry.getColumn());
				}else{
					fail("Test shouldn't have this case!");
				}
			}
			/*assertEquals("WKRPOTEARN", retrieveConstantAndVariable.get("WPE.CLAIMCYCLEID = :claimCycleID").get("CLAIMCYCLEID").get(0));
			assertEquals("WKRPOTEARN", retrieveConstantAndVariable.get("WPE.WKRPOTEARNSTTSCD = 'Finalized'").get("WKRPOTEARNSTTSCD").get(0));
			assertEquals("WKRPOTMTCALCEARN", retrieveConstantAndVariable.get("WPMCE.EARNAMTTYPECD = :earnAmtTypeCD").get("EARNAMTTYPECD").get(0));
			assertEquals("WKRPOTEARN", retrieveConstantAndVariable.get("WPE.RECORDSTATUSCODE = 'RST1'").get("RECORDSTATUSCODE").get(0));
			assertEquals("WKRPOTMTCALCEARN", retrieveConstantAndVariable.get("WPMCE.RECORDSTATUSCODE = 'RST1'").get("RECORDSTATUSCODE").get(0));
			assertEquals("CLAIMCYCLE", retrieveConstantAndVariable.get("CC.RECORDSTATUSCODE = 'RST1'").get("RECORDSTATUSCODE").get(0));
			assertEquals("OCCCLSAVGEARN", retrieveConstantAndVariable.get("OCCCLSAVGREGIONCD <> :regionCode").get("OCCCLSAVGREGIONCD").get(0));
			assertEquals("WKRPOTMTCALCEARN", retrieveConstantAndVariable.get("YEAR(WPMCE.EARNCOLLECTYR) = :startDt").get("EARNCOLLECTYR").get(0));
			assertEquals("WKRPOTEARN", retrieveConstantAndVariable.get("WPE.WKRPOTENARNTYPECD IN ('OCA', 'Actual Earnings')").get("WKRPOTENARNTYPECD").get(0));*/
		} catch (SQLParserException | SQLParserInternalException e) {
			e.printStackTrace();
		}


	}



}
