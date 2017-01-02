package com.universeprojects.miniup.server.dao;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.domain.Location;

import helper.utilities.HttpServletRequestImpl;

public class LocationDaoTest {

	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

	private LocationDao testObj;

	@Before
	public void before() {
		helper.setUp();
		CachedDatastoreService.disableRemoteAPI();
		testObj = new LocationDao(new ODPDBAccess(new HttpServletRequestImpl()).getDB());
	}

	@After
	public void after() {
		helper.tearDown();
	}

	@Test (expected = AssertionError.class)
	public void nullCachedEntity() {
		new Location(null);
	}

	@Test
	public void saveAndGet() { // This works because of the caching
		Location expectedLocation = new Location();

		testObj.save(expectedLocation);
		Location actualLocation = testObj.get(expectedLocation.getCachedEntity().getKey());

		assertEquals(expectedLocation, actualLocation);
	}
}
