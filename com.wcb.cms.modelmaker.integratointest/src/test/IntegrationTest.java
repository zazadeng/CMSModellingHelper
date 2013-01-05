package test;

import java.io.IOException;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.wcb.cms.modelmaker.api.AppInterface;
import com.wcb.cms.modelmaker.api.CMSRoseModellingResult;

public class IntegrationTest {

	private AppInterface application;

	public IntegrationTest(String dbPath){
		init();
		runCase(dbPath);
	}
	public void init(){
		try {
			InitialContext ctx = new InitialContext();
			String jndiName = "osgi:service/"+AppInterface.class.getName();
			application = (AppInterface) ctx.lookup(jndiName);
			System.out.println();
		} catch (NamingException e) {
			System.err.println("Can't find name::::::::::::::: "+ e);

		}
	}
	public void runCase(String dbPath){
		/**
		 * CASE
		 */
		String sql  = "Select claimcycleid from wcoclaim where claimid = :cID";
		try {
			application.transformSelectQuery(sql, dbPath);
		} catch (IOException e) {
			System.out.println("True... Why: claimcycleid is not in wcoclaim.");
		}catch(Exception e){
			//Unexpected
			e.printStackTrace();
		}

		/**
		 * CASE
		 */
		sql  = "Select claimcycleid from claimcycle where claimid = :cID";
		try {
			CMSRoseModellingResult result = application.transformSelectQuery(sql, dbPath);
			//assertTrue(result.getCuramNonStandardSelectQuery().matches(".+:claimCycleId\\sfrom.+"));
			System.out.println(result.getCuramNonStandardSelectQuery());
			if(result.getCuramNonStandardSelectQuery().contains(":claimCycleId_a")){
				System.out.println("YES ... ");
			}else{
				System.err.println("NO... something wrong");
			}
			if(result.getCuramNonStandardSelectQuery().contains(":cID_b")){
				System.out.println("YES ... ");
			}else{
				System.err.println("NO... something wrong");
			}
			if(result.getInputStruct().size() == 1){
				System.out.println("YES ... ");
			}else{
				System.err.println("NO... something wrong");
			}
			if(result.getOutputStruct().size() == 1){
				System.out.println("YES ... ");
			}else{
				System.err.println("NO... something wrong");
			}
			if(result.getOutputStruct().get("claimCycleId_a").equals("CLAIM_CYCLE_ID")){
				System.out.println("YES ... ");
			}else{
				System.err.println("NO... but we get this \"" + result.getOutputStruct().get("claimCycleId_a") + "\".");
			}
			if(result.getInputStruct().get("cID_b").equals("CLAIM_ID")){
				System.out.println("YES ... ");
			}else{
				System.err.println("NO... but we get this \"" + result.getInputStruct().get("cID_b") + "\".");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}catch(Exception e){
			//Unexpected
			e.printStackTrace();
		}
	}
}
