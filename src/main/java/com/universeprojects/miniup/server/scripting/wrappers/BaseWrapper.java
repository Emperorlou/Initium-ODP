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
}
