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
import com.universeprojects.miniup.server.domain.TransmuteRecipe;

import helper.utilities.HttpServletRequestImpl;

public class TransmuteRecipeDaoTest {

	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

	private TransmuteRecipeDao testObj;

	@Before
	public void before() {
		helper.setUp();
		CachedDatastoreService.disableRemoteAPI();
		testObj = new TransmuteRecipeDao(new ODPDBAccess(new HttpServletRequestImpl()).getDB());
	}

	@After
	public void after() {
		helper.tearDown();
	}

	@Test(expected = AssertionError.class)
	public void nullCachedEntity() {
		new TransmuteRecipe(null);
	}

	@Test
	public void saveAndGet() { // This works because of the caching
		TransmuteRecipe expectedTransmuteRecipe = new TransmuteRecipe();

		testObj.save(expectedTransmuteRecipe);
		TransmuteRecipe actualTransmuteRecipe = testObj.get(expectedTransmuteRecipe.getCachedEntity().getKey());

		assertEquals(expectedTransmuteRecipe, actualTransmuteRecipe);
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
