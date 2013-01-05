package com.wcb.cms.modelmaker.redis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
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
	public void testAddAttributeAndDomainDefinition() {
		List<CMSEntityEntry> testList = new ArrayList<>();
		CMSEntityEntry entry = new CMSEntityEntry();
		entry.setColumn("CLAIMCYCLEID");
		entry.setPotentialTableList(Collections.singletonList("Claimcycle".toUpperCase()));
		testList.add(entry);

		redisConnector.findInDB(testList);
		try {
			redisConnector.addAttributeAndDomainDefinition(testList);
			for (CMSEntityEntry cmsEntityEntry : testList) {
				assertEquals("CLAIM_CYCLE_ID", cmsEntityEntry.getDomainDefinition());
				assertEquals("claimCycleId", cmsEntityEntry.getAttribute());
			}
		} catch (InterruptedException | ExecutionException | IOException e) {
			e.printStackTrace();
		}

	}

	@Test(expected = IOException.class)
	public void testAddDomainDefinition_IOException() throws Exception{
		List<CMSEntityEntry> testList = new ArrayList<>();
		CMSEntityEntry entry = new CMSEntityEntry();

		entry.setFutureDBValue(makeMockFuture("Inncorrct Format"));
		testList.add(entry);

		redisConnector.addAttributeAndDomainDefinition(testList);

	}

	@Test
	public void testFindInDB() {
		List<CMSEntityEntry> testList = new ArrayList<>();
		CMSEntityEntry entry = new CMSEntityEntry();
		entry.setColumn("CLAIMCYCLEID");
		entry.setPotentialTableList(Collections.singletonList("Claimcycle".toUpperCase()));
		testList.add(entry);

		redisConnector.findInDB(testList);

		for (CMSEntityEntry cmsEntityEntry : testList) {
			assertTrue(cmsEntityEntry.getFutureDBReturnValue() != null);
		}
	}

	@Test
	public void testFindInDB_NotFound() {
		List<CMSEntityEntry> testList = new ArrayList<>();
		CMSEntityEntry entry = new CMSEntityEntry();
		entry.setColumn("CLAIMCYCLEIDDDDD");
		entry.setPotentialTableList(Collections.singletonList("Claimcycle".toUpperCase()));
		testList.add(entry);

		redisConnector.findInDB(testList);

		for (CMSEntityEntry cmsEntityEntry : testList) {
			assertTrue(cmsEntityEntry.getFutureDBReturnValue() == null);
		}
	}

}
