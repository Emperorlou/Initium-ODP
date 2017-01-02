package com.universeprojects.miniup.server.dao;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.domain.DefenceStructure;

import helper.utilities.HttpServletRequestImpl;

public class DefenceStructureDaoTest {

	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

	private DefenceStructureDao testObj;

	@Before
	public void before() {
		helper.setUp();
		CachedDatastoreService.disableRemoteAPI();
		testObj = new DefenceStructureDao(new ODPDBAccess(new HttpServletRequestImpl()).getDB());
	}

	@After
	public void after() {
		helper.tearDown();
	}

	@Test (expected = AssertionError.class)
	public void nullCachedEntity() {
		new DefenceStructure(null);
	}

	@Test
	public void saveAndGet() { // This works because of the caching
		DefenceStructure expectedDefenceStructure = new DefenceStructure();

		testObj.save(expectedDefenceStructure);
		DefenceStructure actualDefenceStructure = testObj.get(expectedDefenceStructure.getCachedEntity().getKey());

		assertEquals(expectedDefenceStructure, actualDefenceStructure);
	}
}
