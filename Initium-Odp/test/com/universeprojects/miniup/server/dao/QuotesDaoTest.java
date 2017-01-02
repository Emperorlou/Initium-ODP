package com.universeprojects.miniup.server.dao;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.domain.Quotes;

import helper.utilities.HttpServletRequestImpl;

public class QuotesDaoTest {

	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

	private QuotesDao testObj;

	@Before
	public void before() {
		helper.setUp();
		CachedDatastoreService.disableRemoteAPI();
		testObj = new QuotesDao(new ODPDBAccess(new HttpServletRequestImpl()).getDB());
	}

	@After
	public void after() {
		helper.tearDown();
	}

	@Test
	public void saveAndGet() { // This works because of the caching
		Quotes expectedQuotes = new Quotes();

		testObj.save(expectedQuotes);
		Quotes actualQuotes = testObj.get(expectedQuotes.getCachedEntity().getKey());

		assertEquals(expectedQuotes, actualQuotes);
	}
}
