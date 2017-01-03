package com.universeprojects.miniup.server.dao;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.domain.ConstructionToolRequirement;

import helper.utilities.HttpServletRequestImpl;

public class ConstructionToolRequirementDaoTest {

	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

	private ConstructionToolRequirementDao testObj;

	@Before
	public void before() {
		helper.setUp();
		CachedDatastoreService.disableRemoteAPI();
		testObj = new ConstructionToolRequirementDao(new ODPDBAccess(new HttpServletRequestImpl()).getDB());
	}

	@After
	public void after() {
		helper.tearDown();
	}

	@Test(expected = AssertionError.class)
	public void nullCachedEntity() {
		new ConstructionToolRequirement(null);
	}

	@Test
	public void saveAndGet() { // This works because of the caching
		ConstructionToolRequirement expectedConstructionToolRequirement = new ConstructionToolRequirement();

		testObj.save(expectedConstructionToolRequirement);
		ConstructionToolRequirement actualConstructionToolRequirement = testObj.get(expectedConstructionToolRequirement.getCachedEntity().getKey());

		assertEquals(expectedConstructionToolRequirement, actualConstructionToolRequirement);
	}

	@Test
	public void findAll() {
		testObj.findAll(); // Testing for no exceptions
	}

	@Test
	public void getByKeys() {
		testObj.get(Collections.<Key>emptyList()); // Testing for no exceptions
	}
}