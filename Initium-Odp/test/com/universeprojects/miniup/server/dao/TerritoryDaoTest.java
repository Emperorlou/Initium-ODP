package com.universeprojects.miniup.server.dao;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.domain.Territory;

import helper.utilities.HttpServletRequestImpl;

public class TerritoryDaoTest {

	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

	private TerritoryDao testObj;

	@Before
	public void before() {
		helper.setUp();
		CachedDatastoreService.disableRemoteAPI();
		testObj = new TerritoryDao(new ODPDBAccess(new HttpServletRequestImpl()).getDB());
	}

	@After
	public void after() {
		helper.tearDown();
	}

	@Test (expected = AssertionError.class)
	public void nullCachedEntity() {
		new Territory(null);
	}

	@Test
	public void saveAndGet() { // This works because of the caching
		Territory expectedTerritory = new Territory();

		testObj.save(expectedTerritory);
		Territory actualTerritory = testObj.get(expectedTerritory.getCachedEntity().getKey());

		assertEquals(expectedTerritory, actualTerritory);
	}
}
