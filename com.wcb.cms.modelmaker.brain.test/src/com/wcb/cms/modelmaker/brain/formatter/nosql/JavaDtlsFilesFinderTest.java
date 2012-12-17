package com.wcb.cms.modelmaker.brain.formatter.nosql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class JavaDtlsFilesFinderTest {

	@Test
	public void testAll(){
		String dirToGetRecords ="C:\\ZAZA\\work\\C_Drive\\ccp\\NextPlus\\CMS\\EJBServer\\build\\svr\\gen\\curam";
		int size = 19670;
		long currentTimeMillis =0; 
		currentTimeMillis = System.currentTimeMillis();
		List<CMSEntityRecord> threadingResult = new JavaDtlsFilesFinder().getFormatedRecords(dirToGetRecords);
		System.out.println("XXXXXXXXXXXXXXX Thead poor TIME:"+ (System.currentTimeMillis() - currentTimeMillis));
		assertEquals(size,threadingResult.size());
		for (CMSEntityRecord cmsEntity : threadingResult) {
			if(!cmsEntity.getAttribute().matches("\\w+")){
				System.out.println(cmsEntity.getAttribute());
				assertTrue(false);
			}
			if(!cmsEntity.getDomain().matches("([A-Z_0-9]+(_)?)+")){
				System.out.println(cmsEntity.getDomain());
				assertTrue(false);
			}
			if(!cmsEntity.getName().matches("\\w+")){
				System.out.println(cmsEntity.getAttribute());
				assertTrue(false);	
			}
		}
	}


}
