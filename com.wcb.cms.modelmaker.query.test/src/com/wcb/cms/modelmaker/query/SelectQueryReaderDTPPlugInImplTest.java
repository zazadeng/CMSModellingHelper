package com.wcb.cms.modelmaker.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import org.eclipse.datatools.sqltools.parsers.sql.SQLParserException;
import org.eclipse.datatools.sqltools.parsers.sql.SQLParserInternalException;
import org.junit.Test;

import com.wcb.cms.modelmaker.api.CMSEntityEntry;

public class SelectQueryReaderDTPPlugInImplTest {


	private final SelectQueryReaderDTPPlugInImpl selectQueryReaderDTPPlugInImpl =new SelectQueryReaderDTPPlugInImpl();


	private void selectCASE() throws SQLParserException, SQLParserInternalException, IOException {
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
	}

	private void selectCOLUMN() throws SQLParserException,
	SQLParserInternalException, IOException {
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
	}

	private void selectFUNCTION() throws SQLParserException,
	SQLParserInternalException, IOException {
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
		}

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
			}else if(cmsEntityEntry.getColumn().equals("INTEGER")){
				assertEquals("CAST ( NULL AS INTEGER) MONTHLYEARNINGS", cmsEntityEntry.getSqlElement());
				assertEquals("MONTHLYEARNINGS", cmsEntityEntry.getColumnAlias());
				assertEquals("FUNCTION:CAST", cmsEntityEntry.getTable());
				assertEquals(1, cmsEntityEntry.getPotentialTableList().size());
				for (String table : cmsEntityEntry.getPotentialTableList()) {
					assertTrue("We should NOT see this table " + table + " in the list",
							table.equals("FUNCTION:CAST"));
				}
			}else{
				fail("We shouldn't see other cases!");
			}
		}
	}

	private void selectSTAR() throws SQLParserException, SQLParserInternalException , IOException {
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
		}

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
			assertEquals("", cmsEntityEntry.getTable());
			assertEquals(2, cmsEntityEntry.getPotentialTableList().size());
			for (String table : cmsEntityEntry.getPotentialTableList()) {
				assertTrue("We should NOT see this table " + table + " in the list",
						table.equals("ClaimCycle".toUpperCase())|| table.equals("wcoclaim".toUpperCase()));
			}
		}

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
			assertEquals("claimcycle".toUpperCase(), cmsEntityEntry.getTable());
			assertEquals(1, cmsEntityEntry.getPotentialTableList().size());
			for (String table : cmsEntityEntry.getPotentialTableList()) {
				assertTrue("We should NOT see this table " + table + " in the list",
						table.equals("ClaimCycle".toUpperCase()));}
		}

		/*
		 * Select:	*
		 * From:	Query Select
		 */
		selectQuery = "select * from (select claimcycleid from claimcycle, caseheader caaaa) cc, wcoclaim wc";
		retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		assertEquals(2, retrieveIntoClause.size());
		for (CMSEntityEntry cmsEntityEntry : retrieveIntoClause) {
			if(cmsEntityEntry.getColumn().equals("*")){
				assertEquals("*", cmsEntityEntry.getSqlElement());
				assertEquals("", cmsEntityEntry.getColumnAlias());
				assertEquals("*", cmsEntityEntry.getColumn());
				assertEquals("wcoclaim".toUpperCase(), cmsEntityEntry.getTable());
				assertEquals("wc".toUpperCase(), cmsEntityEntry.getTableAlias(cmsEntityEntry.getTable()));
				assertEquals(1, cmsEntityEntry.getPotentialTableList().size());
				assertEquals("wcoclaim".toUpperCase(), cmsEntityEntry.getPotentialTableList().get(0));

			}else if(cmsEntityEntry.getColumn().equals("claimcycleid".toUpperCase())){
				assertEquals("*", cmsEntityEntry.getSqlElement());
				assertEquals("", cmsEntityEntry.getColumnAlias());
				assertEquals("", cmsEntityEntry.getTable());//NOT set, cuz we have more than one!
				assertEquals("claimcycleid".toUpperCase(), cmsEntityEntry.getColumn());
				assertEquals("cc".toUpperCase(), cmsEntityEntry.getTableAlias("claimcycle".toUpperCase()));
				assertEquals(2, cmsEntityEntry.getPotentialTableList().size());
				for (String table : cmsEntityEntry.getPotentialTableList()) {
					assertTrue("We should NOT see this table " + table + " in the list",
							table.equals("ClaimCycle".toUpperCase()) || table.equals("caseheader".toUpperCase()));
				}
			}else{
				fail("We shouldn't see other cases!");
			}
		}

		/*
		 * Select:	*
		 * From:	Query Select, two "cc", DB2 will NOT complain ...
		 * but we should throw an error, because later when we try to replace "*" with cc.claimcycleid,
		 * DB2 will complain
		 */
		selectQuery = "select * from (select claimcycleid from claimcycle, caseheader caaaa) cc, claimcycle cc";
		try{
			retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		}catch(Exception e){
			//TODO: throw ambiguity query exception
		}

		/*
		 * Select:	*
		 * From:	Query Select, two "cc", DB2 will complain ...
		 * and we should as well ...
		 */
		selectQuery = "select cc.claimcycleid from (select claimcycleid from claimcycle, caseheader caaaa) cc, claimcycle cc";
		try{
			retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		}catch(Exception e){
			//TODO: throw ambiguity query exception
		}
		/*
		 * Select:	cc.claimcycleid, that is fine. no ambiguity ...
		 * and we should as well ...
		 */
		selectQuery = "select cc1.claimcycleid from (select claimcycleid from claimcycle, caseheader caaaa) cc1, claimcycle cc2";
		retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		assertEquals(1, retrieveIntoClause.size());
		for (CMSEntityEntry cmsEntityEntry : retrieveIntoClause) {
			if(cmsEntityEntry.getColumn().equals("claimcycleid".toUpperCase())){
				assertEquals("cc1.claimcycleid", cmsEntityEntry.getSqlElement());
				assertEquals("", cmsEntityEntry.getColumnAlias());
				assertEquals("", cmsEntityEntry.getTable());
				assertEquals(2, cmsEntityEntry.getPotentialTableList().size());
				assertEquals("CC1", cmsEntityEntry.getTableAlias("claimcycle".toUpperCase()));
				assertEquals("CC1", cmsEntityEntry.getTableAlias("caseheader".toUpperCase())); //NOT "caaaa"
				for (String table : cmsEntityEntry.getPotentialTableList()) {
					assertTrue("We should NOT see this table " + table + " in the list",
							table.equals("ClaimCycle".toUpperCase()) || table.equals("caseheader".toUpperCase()));
				}
			}else{
				fail("We shouldn't see other cases!");
			}
		}



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
		}
	}

	private void selectTableSTAR() throws SQLParserException, SQLParserInternalException, IOException {
		/*
		 * Select: 	all table columns
		 * from: 	Table in database
		 * 
		 */
		String selectQuery = "select cc.* from ClaimCycle cc";
		List<CMSEntityEntry> retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		assertEquals(1, retrieveIntoClause.size());
		for (CMSEntityEntry cmsEntityEntry : retrieveIntoClause) {
			assertEquals("cc.*", cmsEntityEntry.getSqlElement());
			assertEquals("*", cmsEntityEntry.getColumn());
			assertEquals("ClaimCycle".toUpperCase(), cmsEntityEntry.getTable());
			assertEquals("CC", cmsEntityEntry.getTableAlias(cmsEntityEntry.getTable()));
			assertEquals(1, cmsEntityEntry.getPotentialTableList().size());
			for (String table : cmsEntityEntry.getPotentialTableList()) {
				assertTrue("We should NOT see this table " + table + " in the list",
						table.equals("ClaimCycle".toUpperCase()));
			}
		}

		/*
		 * Select: 	all table columns
		 * from: 	Query Select
		 * 
		 */
		selectQuery = "select cc.* from (select * from claimcycle) cc";
		retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		assertEquals(1, retrieveIntoClause.size());
		for (CMSEntityEntry cmsEntityEntry : retrieveIntoClause) {
			assertEquals("cc.*", cmsEntityEntry.getSqlElement());
			assertEquals("*", cmsEntityEntry.getColumn());
			assertEquals("ClaimCycle".toUpperCase(), cmsEntityEntry.getTable());
			assertEquals("CC", cmsEntityEntry.getTableAlias(cmsEntityEntry.getTable()));
			assertEquals(1, cmsEntityEntry.getPotentialTableList().size());
			for (String table : cmsEntityEntry.getPotentialTableList()) {
				assertTrue("We should NOT see this table " + table + " in the list",
						table.equals("ClaimCycle".toUpperCase()));
			}
		}

		/*
		 * Select: 	all table columns
		 * from: 	Query Select
		 * 
		 */
		selectQuery = "select cc.* from (select claimcycleid from claimcycle) cc";
		retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		assertEquals(2, retrieveIntoClause.size());
		for (CMSEntityEntry cmsEntityEntry : retrieveIntoClause) {
			if("claimcycleid".toUpperCase().equals(cmsEntityEntry.getColumn())){
				assertEquals("cc.*", cmsEntityEntry.getSqlElement());
				assertEquals("ClaimCycle".toUpperCase(), cmsEntityEntry.getTable());
				assertEquals("CC", cmsEntityEntry.getTableAlias(cmsEntityEntry.getTable()));
				assertEquals(1, cmsEntityEntry.getPotentialTableList().size());
				for (String table : cmsEntityEntry.getPotentialTableList()) {
					assertTrue("We should NOT see this table " + table + " in the list",
							table.equals("ClaimCycle".toUpperCase()));
				}
			}else if("*".equals(cmsEntityEntry.getColumn())){
				assertEquals("cc.*", cmsEntityEntry.getSqlElement());
				assertEquals("", cmsEntityEntry.getTable());
				assertTrue(cmsEntityEntry.getTableAlias(cmsEntityEntry.getTable()) == null);
				assertEquals(0, cmsEntityEntry.getPotentialTableList().size());
			}else{
				fail("we should not have this column: "+ cmsEntityEntry.getColumn());
			}
		}

		/*
		 * Select: 	all table columns
		 * from: 	Table in database
		 * kk has no associated table
		 */
		selectQuery = "select kk.* from ClaimCycle cc";
		try{
			retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
			fail("TEST should not reach here!");
		}catch(Exception e){
			assertTrue(true);
		}

		/*
		 * Select: 	all table columns
		 * 
		 */
		selectQuery = "select cc.*, wc.* from ClaimCycle cc inner join wcoclaim wc on wc.claimid=cc.claimid inner join caseheader ch on ch.caseid = cc.claimcycleid";
		retrieveIntoClause = selectQueryReaderDTPPlugInImpl.retrieveIntoClause(selectQuery);
		assertEquals(2, retrieveIntoClause.size());
		for (CMSEntityEntry cmsEntityEntry : retrieveIntoClause) {
			if(cmsEntityEntry.getSqlElement().equals("cc.*")){
				assertEquals("cc.*", cmsEntityEntry.getSqlElement());
				assertEquals("*", cmsEntityEntry.getColumn());
				assertEquals("ClaimCycle".toUpperCase(), cmsEntityEntry.getTable());
				assertEquals("CC", cmsEntityEntry.getTableAlias(cmsEntityEntry.getTable()));
				assertEquals(1, cmsEntityEntry.getPotentialTableList().size());
				for (String table : cmsEntityEntry.getPotentialTableList()) {
					assertTrue("We should NOT see this table " + table + " in the list",
							table.equals("ClaimCycle".toUpperCase()));
				}
			}else if(cmsEntityEntry.getSqlElement().equals("wc.*")){
				assertEquals("wc.*", cmsEntityEntry.getSqlElement());
				assertEquals("*", cmsEntityEntry.getColumn());
				assertEquals("wcoclaim".toUpperCase(), cmsEntityEntry.getTable());
				assertEquals("WC", cmsEntityEntry.getTableAlias(cmsEntityEntry.getTable()));
				assertEquals(1, cmsEntityEntry.getPotentialTableList().size());
				for (String table : cmsEntityEntry.getPotentialTableList()) {
					assertTrue("We should NOT see this table " + table + " in the list",
							table.equals("wcoclaim".toUpperCase()));
				}
			}else{
				fail("We shouldn't see this fragment: "+ cmsEntityEntry.getSqlElement());
			}
		}

	}

	@Test
	public void testGetTableColumnsSet() {

		try {
			selectSTAR();
			selectTableSTAR();
			selectFUNCTION();
			selectCOLUMN();
			selectCASE();

		} catch (SQLParserException | SQLParserInternalException | IOException e) {
			e.printStackTrace();
			fail("Ooopss... should not happen!");
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
		} catch (SQLParserException | SQLParserInternalException e) {
			e.printStackTrace();
		}


	}



}
