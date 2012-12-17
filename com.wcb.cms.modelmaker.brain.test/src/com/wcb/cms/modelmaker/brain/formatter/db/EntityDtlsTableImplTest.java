package com.wcb.cms.modelmaker.brain.formatter.db;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Test;

public class EntityDtlsTableImplTest {

	@Test
	public void testGetRecordsFrom() {
		String dirToGetRecords ="C:\\ZAZA\\work\\C_Drive\\ccp\\NextPlus\\CMS\\EJBServer\\build\\svr\\gen\\curam";
		long currentTimeMillis = System.currentTimeMillis();
		List<Map<String, String>> recordsFrom = new EntityDtlsTableImpl().getFormatedRecords(dirToGetRecords );
		System.out.println("TIME:"+ (System.currentTimeMillis() - currentTimeMillis));
		assertEquals(19670, recordsFrom.size());
	}

}
