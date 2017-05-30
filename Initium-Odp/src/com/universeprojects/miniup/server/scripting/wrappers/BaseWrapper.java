package com.universeprojects.miniup.server.scripting.wrappers;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;

public abstract class BaseWrapper {

	public CachedEntity wrappedEntity;
	
	protected Object getProperty(String propertyName)
	{
		return wrappedEntity.hasProperty(propertyName) ? wrappedEntity.getProperty(propertyName) : null;
	}
	
	protected void setProperty(String propertyName, Object propValue)
	{
		wrappedEntity.setProperty(propertyName, propValue);
	}

	/**
	 * Allows us to get the raw entity. Casting is only possible in Java context,
	 * and the override on EntityWrapper only allows it in specific instances. This
	 * lets us handle the raw entity in Java world, without giving access in Script
	 * world.
	 * @return
	 */
	public CachedEntity getEntity()
	{
		return wrappedEntity;
	}
}
