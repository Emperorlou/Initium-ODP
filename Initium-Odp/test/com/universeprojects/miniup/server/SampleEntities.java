package com.universeprojects.miniup.server;

import com.universeprojects.cacheddatastore.CachedEntity;

public class SampleEntities
{
	
	public CachedEntity newBasicPlayerCharacter()
	{
		CachedEntity c = new CachedEntity("Character");
		c.setProperty("type", "PC");
		c.setProperty("name", "Test Character");
		c.setProperty("strength", 5d);
		c.setProperty("dexterity", 5d);
		c.setProperty("intelligence", 5d);
		
		
		
		return c;
	}
	
	
	public CachedEntity newItem(String name)
	{
		return newItem(name, null, null, null);
	}
	
	public CachedEntity newItem(String name, String itemClass, Long weight, Long space)
	{
		CachedEntity item = new CachedEntity("Item");
		
		item.setProperty("name", name);
		item.setProperty("itemClass", itemClass);
		item.setProperty("weight", weight);
		item.setProperty("space", space);
		
		return item;
	}
	
	
	public CachedEntity newGenericAffector(String name, String sourceFieldName, double sourceFieldMinimumValue, double sourceFieldMaximumValue,
			String destinationFieldName, double minimumMultiplier, double maximumMultiplier)
	{
		CachedEntity e = new CachedEntity("GenericAffector");

		e.setProperty("name", name);
		e.setProperty("sourceFieldName", sourceFieldName);
		e.setProperty("sourceFieldMinimumValue", sourceFieldMinimumValue);
		e.setProperty("sourceFieldMaximumValue", sourceFieldMaximumValue);
		e.setProperty("destinationFieldName", destinationFieldName);
		e.setProperty("minimumMultiplier", minimumMultiplier);
		e.setProperty("maximumMultiplier", maximumMultiplier);
		
		return e;
	}
	
}
