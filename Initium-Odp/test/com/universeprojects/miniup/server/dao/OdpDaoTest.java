package com.universeprojects.miniup.server.dao;

import org.junit.After;
import org.junit.Before;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.universeprojects.cacheddatastore.CachedDatastoreService;

public class OdpDaoTest {

	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

	@Before
	public void before() {
		helper.setUp();
		CachedDatastoreService.disableRemoteAPI();
//		testObj = new OdpDao(ODPDBAccess.getInstance(new HttpServletRequestImpl()).getDB());
	}

	@After
	public void after() {
		helper.tearDown();
	}

	//TODO - test common method for Daos
}
