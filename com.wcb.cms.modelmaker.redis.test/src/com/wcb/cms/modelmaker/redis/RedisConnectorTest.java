package com.wcb.cms.modelmaker.redis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wcb.cms.modelmaker.api.CMSEntityEntry;

public class RedisConnectorTest {

	static RedisConnector redisConnector;

	@AfterClass
	public static void runAfterClass() {
		redisConnector.close();
	}

	@BeforeClass
	public static void runBeforeClass() {
		redisConnector = new RedisConnector();
		redisConnector.connect("localhost");
	}

	private Future<String> makeMockFuture(final String value) {
		Future<String> future = new Future<String>() {

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return false;
			}

			@Override
			public String get() throws InterruptedException, ExecutionException {
				return value;
			}

			@Override
			public String get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
				return null;
			}

			@Override
			public boolean isCancelled() {
				return false;
			}

			@Override
			public boolean isDone() {
				return false;
			}
		};
		return future;
	}

	@Test
	public void testAddAttributeAndDomainDefinition() throws Exception {
		List<CMSEntityEntry> testList = new ArrayList<>();
		CMSEntityEntry entry = new CMSEntityEntry("CLAIMCYCLEID");
		entry.addToPotentialTableList(Collections.singletonList("Claimcycle".toUpperCase()));
		testList.add(entry);

		redisConnector.findInDB(testList);
		redisConnector.addAttributeAndDomainDefinition(testList);
		for (CMSEntityEntry cmsEntityEntry : testList) {
			assertEquals("CLAIM_CYCLE_ID", cmsEntityEntry.getDomainDefinition());
			assertEquals("ClaimCycle.claimCycleId", cmsEntityEntry.getEntityAttribute());
		}

	}
	@Test(expected = IOException.class)
	public void testAddAttributeAndDomainDefinition_DBRecordNotFound() throws Exception{
		List<CMSEntityEntry> testList = new ArrayList<>();
		CMSEntityEntry entry = new CMSEntityEntry("CLAIMCYCLEIDDDDDDDDD");
		entry.addToPotentialTableList(Collections.singletonList("Claimcycle".toUpperCase()));
		testList.add(entry);

		redisConnector.findInDB(testList);
		redisConnector.addAttributeAndDomainDefinition(testList);

	}

	@Test
	public void testAddAttributeAndDomainDefinition_manyRecords() throws Exception {
		List<CMSEntityEntry> testList = new ArrayList<>();
		CMSEntityEntry entry = new CMSEntityEntry("*");
		entry.addToPotentialTableList(Collections.singletonList("APRVLLVEL"));
		testList.add(entry);

		redisConnector.findInDB(testList);
		for (CMSEntityEntry cmsEntityEntry : testList) {
			List<Future<String>> futures = cmsEntityEntry.getFutureDBReturnValueList();
			assertEquals(10, futures.size());
		}
		redisConnector.addAttributeAndDomainDefinition(testList);
		for (CMSEntityEntry cmsEntityEntry : testList) {
			assertEquals("APPROVAL_GROUP_ID", cmsEntityEntry.getDomainDefinition("AprvlLvel.aprvlGrpID"));
			assertEquals("BUSINESS_ADDED_USERNAME", cmsEntityEntry.getDomainDefinition("AprvlLvel.busAddedUserID"));
			assertEquals("MAXIMUM_UNITS", cmsEntityEntry.getDomainDefinition("AprvlLvel.maximumUnits"));
			assertEquals("BUSINESS_LAST_UPDATED_USERNAME", cmsEntityEntry.getDomainDefinition("AprvlLvel.busLstUpdUserID"));
			assertEquals("APPROVAL_ID", cmsEntityEntry.getDomainDefinition("AprvlLvel.approvalID"));
		}

	}

	@Test(expected = IOException.class)
	public void testAddDomainDefinition_DBRecordIncorrectFormat() throws Exception{
		List<CMSEntityEntry> testList = new ArrayList<>();
		CMSEntityEntry entry = new CMSEntityEntry("*");

		entry.addFutureDBValue(makeMockFuture("Inncorrct Format"));
		testList.add(entry);

		redisConnector.addAttributeAndDomainDefinition(testList);

	}
	@Test
	public void testFindInDB() {
		List<CMSEntityEntry> testList = new ArrayList<>();
		CMSEntityEntry entry = new CMSEntityEntry("CLAIMCYCLEID");
		entry.addToPotentialTableList(Collections.singletonList("Claimcycle".toUpperCase()));
		testList.add(entry);

		redisConnector.findInDB(testList);

		for (CMSEntityEntry cmsEntityEntry : testList) {
			assertTrue(cmsEntityEntry.getFutureDBReturnValueList() != null);
		}
	}
	@Test
	public void testFindInDB_manyTables() {
		List<CMSEntityEntry> testList = new ArrayList<>();
		CMSEntityEntry entry = new CMSEntityEntry("APPROVALID");
		entry.addToPotentialTableList(Arrays.asList(
				"APRVLLVEL",//ONLY THIS TABLE HAS THE COLUMN
				"ACTIVITY"
				));
		testList.add(entry);

		redisConnector.findInDB(testList);

		for (CMSEntityEntry cmsEntityEntry : testList) {
			List<Future<String>> futures = cmsEntityEntry.getFutureDBReturnValueList();
			assertEquals(1, futures.size());
			for (Future<String> future : futures) {
				assertTrue(future != null);
			}
		}
	}
	@Test
	public void testFindInDB_manyTables_star() {
		List<CMSEntityEntry> testList = new ArrayList<>();
		CMSEntityEntry entry = new CMSEntityEntry("*");
		entry.addToPotentialTableList(Arrays.asList(
				"APRVLLVEL",
				"ACTIVITY"
				));
		testList.add(entry);

		redisConnector.findInDB(testList);

		for (CMSEntityEntry cmsEntityEntry : testList) {
			List<Future<String>> futures = cmsEntityEntry.getFutureDBReturnValueList();
			assertEquals(28, futures.size());
			for (Future<String> future : futures) {
				assertTrue(future != null);
			}
		}
	}
	@Test
	public void testFindInDB_NotFound() {
		List<CMSEntityEntry> testList = new ArrayList<>();
		CMSEntityEntry entry = new CMSEntityEntry("CLAIMCYCLEIDDDDD");
		entry.addToPotentialTableList(Collections.singletonList("Claimcycle".toUpperCase()));
		testList.add(entry);

		redisConnector.findInDB(testList);

		for (CMSEntityEntry cmsEntityEntry : testList) {
			assertTrue(cmsEntityEntry.getFutureDBReturnValueList().isEmpty());
		}
	}

	@Test
	public void testFindInDB_star() {
		List<CMSEntityEntry> testList = new ArrayList<>();
		CMSEntityEntry entry = new CMSEntityEntry("*");
		entry.addToPotentialTableList(Arrays.asList(
				"ACTIVITY"
				));
		testList.add(entry);

		redisConnector.findInDB(testList);

		for (CMSEntityEntry cmsEntityEntry : testList) {
			List<Future<String>> futures = cmsEntityEntry.getFutureDBReturnValueList();
			assertEquals(18, futures.size());
			for (Future<String> future : futures) {
				assertTrue(future != null);
			}
		}
	}
}
