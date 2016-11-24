package com.universeprojects.miniup.server;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;

public class CachedDatastoreServiceTest
{

	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

	@Before
	public void setUp() {
		helper.setUp();
	}
	
	@After
	public void tearDown() {
		helper.tearDown();
	}	

	@Test
	public void bulkPutModeTest()
	{
		CachedDatastoreService ds = new CachedDatastoreService();
		CachedEntity character = new CachedEntity("Character");
		CachedEntity item1 = new CachedEntity("Item");
		item1.setPropertyManually("ownerKey", character.getKey());
		CachedEntity item2 = new CachedEntity("Item");
		item2.setPropertyManually("ownerKey", character.getKey());
		character.setPropertyManually("testKey1", item1.getKey());
		character.setPropertyManually("testKey2", item2.getKey());
		
		ds.beginBulkWriteMode();

		Assert.assertEquals("Character(no-id-yet)", character.getKey().toString());
		Assert.assertEquals("Item(no-id-yet)", item1.getKey().toString());
		Assert.assertEquals("Item(no-id-yet)", item2.getKey().toString());
		
		ds.put(character, item1, item2);
		ds.commitBulkWrite();

		Assert.assertEquals(item1.getKey(), character.getProperty("testKey1"));
		Assert.assertEquals(item2.getKey(), character.getProperty("testKey2"));
		Assert.assertEquals(character.getKey(), item1.getProperty("ownerKey"));
		Assert.assertEquals(character.getKey(), item2.getProperty("ownerKey"));
	}
	

}
