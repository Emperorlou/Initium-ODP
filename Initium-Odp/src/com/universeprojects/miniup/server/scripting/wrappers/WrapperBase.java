package com.universeprojects.miniup.server.scripting.wrappers;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

/**
 * Base wrapper class for CachedEntity objects, for use with the Scripting 
 * service. Keeping instance of DB so we can retrieve other entities within
 * the wrapped objects themselves.
 * 
 * @author spfiredrake
 */
public abstract class WrapperBase 
{
	protected CachedEntity wrappedEntity;
	protected ODPDBAccess db;
	
	public WrapperBase(CachedEntity entity, ODPDBAccess db)
	{
		this.db = db;
		this.wrappedEntity = entity;
	}

	protected Object getProperty(String propertyName)
	{
		return wrappedEntity.getProperty(propertyName);
	}
	
	protected void setProperty(String propertyName, Object propValue)
	{
		wrappedEntity.setProperty(propertyName, propValue);
	}
	
	protected Key getKey()
	{
		return wrappedEntity.getKey();
	}
}
