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
import com.universeprojects.miniup.server.domain.MonsterSpawner;

import helper.utilities.HttpServletRequestImpl;

public class MonsterSpawnerDaoTest {

	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

	private MonsterSpawnerDao testObj;

	@Before
	public void before() {
		helper.setUp();
		CachedDatastoreService.disableRemoteAPI();
		testObj = new MonsterSpawnerDao(new ODPDBAccess(new HttpServletRequestImpl()).getDB());
	}

	@After
	public void after() {
		helper.tearDown();
	}

	@Test(expected = AssertionError.class)
	public void nullCachedEntity() {
		new MonsterSpawner(null);
	}

	@Test
	public void saveAndGet() { // This works because of the caching
		MonsterSpawner expectedMonsterSpawner = new MonsterSpawner();

		testObj.save(expectedMonsterSpawner);
		MonsterSpawner actualMonsterSpawner = testObj.get(expectedMonsterSpawner.getCachedEntity().getKey());

		assertEquals(expectedMonsterSpawner, actualMonsterSpawner);
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