package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wcb.cms.modelmaker.api.AppInterface;
import com.wcb.cms.modelmaker.api.CMSRoseModellingResult;
import com.wcb.cms.modelmaker.api.ErrorMessages;

public class IntegrationTest {

	private static AppInterface application;

	@BeforeClass
	public static void init(){
		try {
			InitialContext ctx = new InitialContext();
			String jndiName = "osgi:service/"+AppInterface.class.getName();
			application = (AppInterface) ctx.lookup(jndiName);
		} catch (NamingException e) {
			System.err.println("Can't find service with name::::::::::::::: "+ e);

		}
	}
	@Test
	public void runCase(){
		if(application == null){
			return;
		}

		final String dbPath = "localhost";
		/****************************************************
		 * CASE
		 ****************************************************/
		String sql  = "Select claimcycleid from wcoclaim where claimid = :cID";
		try {
			application.transformSelectQuery(sql, dbPath);
		} catch (IOException e) {
			assertEquals(ErrorMessages.error1("claimcycleid".toUpperCase(),
					"wcoclaim".toUpperCase()), e.getMessage());
		}catch(Exception e){
			//Unexpected
			e.printStackTrace();
		}

		/****************************************************
		 * CASE
		 ****************************************************/
		sql  = "Select claimcycleid from claimcycle where claimid = :cID";
		try {
			CMSRoseModellingResult result = application.transformSelectQuery(sql, dbPath);
			assertTrue("NOT containing. The end sql:\n"+result.getCuramNonStandardSelectQuery(),
					result.getCuramNonStandardSelectQuery().contains(":claimCycleId_a"));
			assertTrue("NOT containing. The end sql:\n"+ result.getCuramNonStandardSelectQuery(),
					result.getCuramNonStandardSelectQuery().contains(":cID_b"));
			assertEquals(1, result.getInputStruct().size());
			assertEquals(1, result.getOutputStruct().size());
			assertEquals("CLAIM_CYCLE_ID", result.getOutputStruct().get("claimCycleId_a"));
			assertEquals("CLAIM_ID", result.getInputStruct().get("cID_b"));
		}catch(Exception e){
			//Unexpected
			e.printStackTrace();
		}
	}
}
