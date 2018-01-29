package com.universeprojects.miniup.server;

import helper.utilities.HttpServletRequestMock;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.universeprojects.cacheddatastore.CachedDatastoreService;

public class ODPServerTestBase {

	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

	public ODPDBAccess db;
	public CachedDatastoreService ds;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void before() {
		helper.setUp();
		CachedDatastoreService.disableRemoteAPI();
		db = new ODPDBAccess(new HttpServletRequestMock());
		ds = db.getDB();
	}

	@After
	public void after() {
		helper.tearDown();
	}

}
