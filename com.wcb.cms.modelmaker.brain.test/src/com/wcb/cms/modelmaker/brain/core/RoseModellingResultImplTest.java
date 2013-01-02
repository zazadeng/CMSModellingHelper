package com.wcb.cms.modelmaker.brain.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.wcb.cms.modelmaker.api.CMSEntityEntry;

public class RoseModellingResultImplTest {

	RoseModellingResultImpl roseModellingResultImpl;
	@Before
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

		CMSEntityEntry entry = new CMSEntityEntry();
		entry.setSqlElement("PL.PLANNEDITEMID");
		entry.setAttribute("plannedItemID");
		entry.setDomainDefinition("OUTPUT_DOMAIN_1");
		outputList.add(entry);

		entry = new CMSEntityEntry();
		entry.setSqlElement("PL.NAME");
		entry.setAttribute("name");
		entry.setDomainDefinition("OUTPUT_DOMAIN_2");
		outputList.add(entry);

		entry = new CMSEntityEntry();
		entry.setSqlElement("PL.DECISNOUTCMTYPECD");
		entry.setAttribute("decisnOutcmTypeCd");
		entry.setDomainDefinition("OUTPUT_DOMAIN_3");
		outputList.add(entry);

		entry = new CMSEntityEntry();
		entry.setSqlElement("SG.NAME");
		entry.setAttribute("name");
		entry.setDomainDefinition("OUTPUT_DOMAIN_4");
		outputList.add(entry);

		List<CMSEntityEntry> inputList = new ArrayList<CMSEntityEntry>();

		//1
		entry = new CMSEntityEntry();
		entry.setSqlElement("C.INTEGRATEDCASEID = :claimID");
		entry.setAttribute("claimID");
		entry.setDomainDefinition("DOMAIN_1");
		inputList.add(entry);
		//2
		entry = new CMSEntityEntry();
		entry.setSqlElement("PL.DECISNOUTCMTYPECD IN ('DOT1','DOT11', 'DOT12')");
		entry.setAttribute("decisnOutcmTypeCd");
		entry.setDomainDefinition("DOMAIN_2");
		inputList.add(entry);
		//3
		entry = new CMSEntityEntry();
		entry.setSqlElement("PL.RECORDSTATUSCODE = 'RST1'");
		entry.setAttribute("recordStatusCode");
		entry.setDomainDefinition("DOMAIN_3");
		inputList.add(entry);
		//4
		entry = new CMSEntityEntry();
		entry.setSqlElement("PL.PLANITEMTYPECODE IN ('S', 'SG')");
		entry.setAttribute("planItemTypeCode");
		entry.setDomainDefinition("DOMAIN_4");
		inputList.add(entry);
		//5
		entry = new CMSEntityEntry();
		entry.setSqlElement("SG.RECORDSTATUS = 'RST1'");
		entry.setAttribute("recordStatus");
		entry.setDomainDefinition("DOMAIN_5");
		inputList.add(entry);
		//6
		entry = new CMSEntityEntry();
		entry.setSqlElement("PNG.RECORDSTATUSCODE = 'RST1'");
		entry.setAttribute("recordStatusCode");
		entry.setDomainDefinition("DOMAIN_6");
		inputList.add(entry);
		//7
		entry = new CMSEntityEntry();
		entry.setSqlElement("PG.RECORDSTATUSCODE   = 'RST1'");
		entry.setAttribute("recordStatusCode");
		entry.setDomainDefinition("DOMAIN_7");
		inputList.add(entry);
		//8
		entry = new CMSEntityEntry();
		entry.setSqlElement("PG.PLANGRPNAMETYPCODE = :planGroupType");
		entry.setAttribute("planGrpNameTypCode");
		entry.setDomainDefinition("DOMAIN_8");
		inputList.add(entry);

		roseModellingResultImpl =
				new RoseModellingResultImpl(selectQuery , outputList , inputList);
	}


	@Test
	public void testGetCuramNonStandardSelectQuery() {
		String result = roseModellingResultImpl.getCuramNonStandardSelectQuery();
		System.out.println(result);
		String expected = "SELECT PL.PLANNEDITEMID    ,\n" +
				"       PL.NAME             ,\n" +
				"       PL.DECISNOUTCMTYPECD,\n" +
				"       SG.NAME\r\n" +
				" INTO \r\n" +
				":plannedItemID_a \r\n" +
				",:name_b \r\n" +
				",:decisnOutcmTypeCd_c \r\n" +
				",:name_d \r\n" +
				"\n" +
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
		Map<String, String> inputStruct = roseModellingResultImpl.getInputStruct();
		assertEquals(11, inputStruct.size());
		assertEquals("DOMAIN_1", inputStruct.get("claimID_e"));
		assertEquals("DOMAIN_2", inputStruct.get("decisnOutcmTypeCd_DOT1_f"));
		assertEquals("DOMAIN_2", inputStruct.get("decisnOutcmTypeCd_DOT11_g"));
		assertEquals("DOMAIN_2", inputStruct.get("decisnOutcmTypeCd_DOT12_h"));
		assertEquals("DOMAIN_5", inputStruct.get("recordStatus_RST1_l"));
		assertEquals("DOMAIN_8", inputStruct.get("planGroupType_o"));

	}

	@Test
	public void testGetOutputStruct() {
		Map<String, String> outputStruct = roseModellingResultImpl.getOutputStruct();
		assertEquals(4, outputStruct.size());
		assertEquals("OUTPUT_DOMAIN_1", outputStruct.get("plannedItemID_a"));
		assertEquals("OUTPUT_DOMAIN_2", outputStruct.get("name_b"));
		assertEquals("OUTPUT_DOMAIN_3", outputStruct.get("decisnOutcmTypeCd_c"));
		assertEquals("OUTPUT_DOMAIN_4", outputStruct.get("name_d"));
	}

}
