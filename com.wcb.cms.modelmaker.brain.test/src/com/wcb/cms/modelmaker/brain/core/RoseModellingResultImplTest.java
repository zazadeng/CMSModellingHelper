package com.wcb.cms.modelmaker.brain.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.wcb.cms.modelmaker.api.CMSEntityEntry;

public class RoseModellingResultImplTest {

	RoseModellingResultImpl roseModellingResultImpl;

	public void beforeClassSetup(){
		String selectQuery = "SELECT PL.PLANNEDITEMID    ,\n" +
				"       PL.NAME             ,\n" +
				"       PL.DECISNOUTCMTYPECD,\n" +
				"       SG.NAME\n" +
				" FROM   PLANNEDITEM PL\n" +
				"       INNER JOIN CASEHEADER C\n" +
				"       ON C.CASEID = PL.CASEID\n" +
				"       AND C.INTEGRATEDCASEID = :claimID\n" +
				"       AND PL.DECISNOUTCMTYPECD IN ('DOT1','DOT11', 'DOT12')\n" +
				"       AND PL.RECORDSTATUSCODE = 'RST1'\n" +
				"       AND PL.PLANITEMTYPECODE IN ('S', 'SG')\n" +
				"       INNER JOIN PLANNEDSUBGOAL SG\n" +
				"       ON     SG.PLANNEDSUBGOALID = PL.PLANNEDSUBGOALID\n" +
				"       AND    SG.RECORDSTATUS = 'RST1'\n" +
				"       INNER JOIN PLANNEDGROUP PNG\n" +
				"       ON PNG.PLANNEDGROUPID = SG.PLANNEDGROUPID\n" +
				"       AND PNG.RECORDSTATUSCODE = 'RST1'\n" +
				"       INNER JOIN PLANGROUP PG\n" +
				"       ON     PG.PLANGROUPID        = PNG.PLANGROUPID\n" +
				"       AND    PG.RECORDSTATUSCODE   = 'RST1'\n" +
				"       AND    PG.PLANGRPNAMETYPCODE = :planGroupType\n" +
				"WITH UR;";

		List<CMSEntityEntry> outputList = new ArrayList<CMSEntityEntry>();

		CMSEntityEntry entry = new CMSEntityEntry("ColumnName");
		entry.setSqlElement("PL.PLANNEDITEMID");
		entry.addToAttrDomainDefMap("plannedItemID", "OUTPUT_DOMAIN_1");
		outputList.add(entry);

		entry = new CMSEntityEntry("ColumnName");
		entry.setSqlElement("PL.NAME");
		entry.addToAttrDomainDefMap("name", "OUTPUT_DOMAIN_2");
		outputList.add(entry);

		entry = new CMSEntityEntry("ColumnName");
		entry.setSqlElement("PL.DECISNOUTCMTYPECD");
		entry.addToAttrDomainDefMap("decisnOutcmTypeCd", "OUTPUT_DOMAIN_3");
		outputList.add(entry);

		entry = new CMSEntityEntry("ColumnName");
		entry.setSqlElement("SG.NAME");
		entry.addToAttrDomainDefMap("name", "OUTPUT_DOMAIN_4");
		outputList.add(entry);

		List<CMSEntityEntry> inputList = new ArrayList<CMSEntityEntry>();

		//1
		entry = new CMSEntityEntry("ColumnName");
		entry.setSqlElement("C.INTEGRATEDCASEID = :claimID");
		entry.addToAttrDomainDefMap("claimID", "DOMAIN_1");
		inputList.add(entry);
		//2
		entry = new CMSEntityEntry("ColumnName");
		entry.setSqlElement("PL.DECISNOUTCMTYPECD IN ('DOT1','DOT11', 'DOT12')");
		entry.addToAttrDomainDefMap("decisnOutcmTypeCd", "DOMAIN_2");
		inputList.add(entry);
		//3
		entry = new CMSEntityEntry("ColumnName");
		entry.setSqlElement("PL.RECORDSTATUSCODE = 'RST1'");
		entry.addToAttrDomainDefMap("recordStatusCode", "DOMAIN_3");
		inputList.add(entry);
		//4
		entry = new CMSEntityEntry("ColumnName");
		entry.setSqlElement("PL.PLANITEMTYPECODE IN ('S', 'SG')");
		entry.addToAttrDomainDefMap("planItemTypeCode", "DOMAIN_4");
		inputList.add(entry);
		//5
		entry = new CMSEntityEntry("ColumnName");
		entry.setSqlElement("SG.RECORDSTATUS = 'RST1'");
		entry.addToAttrDomainDefMap("recordStatus", "DOMAIN_5");
		inputList.add(entry);
		//6
		entry = new CMSEntityEntry("ColumnName");
		entry.setSqlElement("PNG.RECORDSTATUSCODE = 'RST1'");
		entry.addToAttrDomainDefMap("recordStatusCode", "DOMAIN_6");
		inputList.add(entry);
		//7
		entry = new CMSEntityEntry("ColumnName");
		entry.setSqlElement("PG.RECORDSTATUSCODE   = 'RST1'");
		entry.addToAttrDomainDefMap("recordStatusCode", "DOMAIN_7");
		inputList.add(entry);
		//8
		entry = new CMSEntityEntry("ColumnName");
		entry.setSqlElement("PG.PLANGRPNAMETYPCODE = :planGroupType");
		entry.addToAttrDomainDefMap("planGrpNameTypCode", "DOMAIN_8");
		inputList.add(entry);

		roseModellingResultImpl =
				new RoseModellingResultImpl(selectQuery , outputList , inputList);
	}


	@Test
	public void testGetCuramNonStandardSelectQuery() {
		beforeClassSetup();
		String result = roseModellingResultImpl.getCuramNonStandardSelectQuery();
		String expected = "SELECT PL.PLANNEDITEMID    ,\n" +
				"       PL.NAME             ,\n" +
				"       PL.DECISNOUTCMTYPECD,\n" +
				"       SG.NAME\r\n" +
				" INTO \r\n" +
				":plannedItemID_a \r\n" +
				",:name_b \r\n" +
				",:decisnOutcmTypeCd_c \r\n" +
				",:name_d\n" +
				" FROM   PLANNEDITEM PL\n" +
				"       INNER JOIN CASEHEADER C\n" +
				"       ON C.CASEID = PL.CASEID\n" +
				"       AND C.INTEGRATEDCASEID = :claimID_e\n" +
				"       AND PL.DECISNOUTCMTYPECD IN (:decisnOutcmTypeCd_DOT1_f,:decisnOutcmTypeCd_DOT11_g, :decisnOutcmTypeCd_DOT12_h)\n" +
				"       AND PL.RECORDSTATUSCODE = :recordStatusCode_RST1_i\n" +
				"       AND PL.PLANITEMTYPECODE IN (:planItemTypeCode_S_j, :planItemTypeCode_SG_k)\n" +
				"       INNER JOIN PLANNEDSUBGOAL SG\n" +
				"       ON     SG.PLANNEDSUBGOALID = PL.PLANNEDSUBGOALID\n" +
				"       AND    SG.RECORDSTATUS = :recordStatus_RST1_l\n" +
				"       INNER JOIN PLANNEDGROUP PNG\n" +
				"       ON PNG.PLANNEDGROUPID = SG.PLANNEDGROUPID\n" +
				"       AND PNG.RECORDSTATUSCODE = :recordStatusCode_RST1_m\n" +
				"       INNER JOIN PLANGROUP PG\n" +
				"       ON     PG.PLANGROUPID        = PNG.PLANGROUPID\n" +
				"       AND    PG.RECORDSTATUSCODE   = :recordStatusCode_RST1_n\n" +
				"       AND    PG.PLANGRPNAMETYPCODE = :planGroupType_o\n" +
				"WITH UR;";
		assertEquals(expected, result);
		assertTrue(result.matches(".*'\\w+'.*") == false);
	}

	@Test
	public void testGetInputStruct() {
		beforeClassSetup();
		Map<String, String> inputStruct = roseModellingResultImpl.getInputStruct();
		assertEquals(11, inputStruct.size());

		assertEquals("DOMAIN_2", inputStruct.get("decisnOutcmTypeCd_DOT11_g"));
		assertEquals("DOMAIN_2", inputStruct.get("decisnOutcmTypeCd_DOT12_h"));
		assertEquals("DOMAIN_5", inputStruct.get("recordStatus_RST1_l"));
		assertEquals("DOMAIN_8", inputStruct.get("planGroupType_o"));

	}

	@Test
	public void testGetOutputStruct() {
		beforeClassSetup();
		Map<String, String> outputStruct = roseModellingResultImpl.getOutputStruct();
		assertEquals(4, outputStruct.size());
		assertEquals("OUTPUT_DOMAIN_1", outputStruct.get("plannedItemID_a"));
		assertEquals("OUTPUT_DOMAIN_2", outputStruct.get("name_b"));
		assertEquals("OUTPUT_DOMAIN_3", outputStruct.get("decisnOutcmTypeCd_c"));
		assertEquals("OUTPUT_DOMAIN_4", outputStruct.get("name_d"));
	}

	@Test
	public void testSTARCase(){
		String selectQuery = "select * from (select claimid from wcoclaim) w, caseHeader where w.claimid = :id";
		List<CMSEntityEntry> outputList = new ArrayList<CMSEntityEntry>();

		CMSEntityEntry entry = new CMSEntityEntry("*");
		entry.setSqlElement("*");
		entry.addToPotentialTableList(Arrays.asList("caseHeader".toUpperCase()));//DONE in reading query
		entry.addToAttrDomainDefMap("CaseHeader.caseID", "OUTPUT_DOMAIN_1");//DONE in db access
		entry.addToAttrDomainDefMap("CaseHeader.recordStatus", "OUTPUT_DOMAIN_2");//DONE in db access
		outputList.add(entry);

		entry = new CMSEntityEntry("claimid".toUpperCase());
		entry.setSqlElement("*");
		entry.addToPotentialTableList(Arrays.asList("WCOCLAIM(W)"));//DONE in reading query
		entry.addToAttrDomainDefMap("WCOClaim.claimID", "OUTPUT_DOMAIN_3");//DONE in db access
		outputList.add(entry);

		List<CMSEntityEntry> inputList = new ArrayList<CMSEntityEntry>();

		entry = new CMSEntityEntry("ColumnName");
		entry.setSqlElement("w.claimid = :id");
		entry.addToAttrDomainDefMap("id", "DOMAIN_1");
		inputList.add(entry);

		roseModellingResultImpl =
				new RoseModellingResultImpl(selectQuery , outputList , inputList);
		String result = roseModellingResultImpl.getCuramNonStandardSelectQuery();
		System.out.println(result);
		String expected = "select W.claimID\n" +
				",CASEHEADER.recordStatus\n" +
				",CASEHEADER.caseID\r\n" +
				" INTO \r\n:claimID_c \r\n" +
				",:recordStatus_a \r\n" +
				",:caseID_b\n" +
				" from (select claimid from wcoclaim) w, caseHeader where w.claimid = :id_d";
		assertEquals(expected , result);
		Map<String, String> map = roseModellingResultImpl.getOutputStruct();
		assertEquals(3, map.size());
		for (String key : map.keySet()) {
			assertTrue(map.get(key).startsWith("OUTPUT") == true);
		}

		map = roseModellingResultImpl.getInputStruct();
		assertEquals(1, map.size());
		assertEquals("DOMAIN_1", map.get("id_d"));

	}
}
