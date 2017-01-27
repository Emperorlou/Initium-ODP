package com.universeprojects.miniup.server.services;

import junit.framework.Assert;

import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.SampleEntities;
import com.universeprojects.miniup.server.ServerTestBase;
import com.universeprojects.miniup.server.services.InventionService.GenericAffectorResult;

public class InventionServiceTest extends ServerTestBase
{

	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

	@Test
	public void processGenericAffectorTest()
	{
		SampleEntities entities = new SampleEntities();
		CachedEntity character = entities.newBasicPlayerCharacter();
		
		CachedEntity club = entities.newItem("Small Wooden Club", "Club", 300L, 300L);
		
		CachedEntity genericAffector = entities.newGenericAffector("Every 300g of club makes 1 kindling", 
				"weight", 300d, 30000d, 
				"quantity", 1d, 100d);
		
		InventionService service = new InventionService(db, character);
		
		// Basic test
		GenericAffectorResult result = service.processGenericAffector(club, genericAffector);
		Assert.assertEquals(1d, result.resultMultiplier);
		
		// Now test with more weight
		club.setProperty("weight", 700L);
		result = service.processGenericAffector(club, genericAffector);
		Assert.assertEquals(2d, Math.floor(result.resultMultiplier));
		
		// Now test with less weight
		club.setProperty("weight", 200L);
		result = service.processGenericAffector(club, genericAffector);
		Assert.assertEquals(1d, Math.floor(result.resultMultiplier));
		
		
		// Try testing an inverted source field range on the affector
		genericAffector.setProperty("sourceFieldMinimumValue", 30000d);
		genericAffector.setProperty("sourceFieldMaximumValue", 300d);
		club.setProperty("weight", 300L);
		result = service.processGenericAffector(club, genericAffector);
		Assert.assertEquals(100d, Math.floor(result.resultMultiplier));
		
		// And testing the other extreme
		club.setProperty("weight", 30000L);
		result = service.processGenericAffector(club, genericAffector);
		Assert.assertEquals(1d, Math.floor(result.resultMultiplier));

		
		// Now try inverting the destination multiplier range as well to see that work...
		genericAffector.setProperty("minimumMultiplier", 100d);
		genericAffector.setProperty("maximumMultiplier", 1d);
		club.setProperty("weight", 30000L);
		result = service.processGenericAffector(club, genericAffector);
		Assert.assertEquals(100d, Math.floor(result.resultMultiplier));

		// And test the other extreme
		club.setProperty("weight", 300L);
		result = service.processGenericAffector(club, genericAffector);
		Assert.assertEquals(1d, Math.floor(result.resultMultiplier));

		// Test source weight below threshold..
		club.setProperty("weight", 0L);
		result = service.processGenericAffector(club, genericAffector);
		Assert.assertEquals(1d, Math.floor(result.resultMultiplier));

		// Test source weight above threshold..
		club.setProperty("weight", 40000L);
		result = service.processGenericAffector(club, genericAffector);
		Assert.assertEquals(100d, Math.floor(result.resultMultiplier));
	}

}
