package com.universeprojects.miniup.server;

import com.universeprojects.cacheddatastore.CachedEntity;

public abstract class InitiumAspect
{
	final protected ODPDBAccess db;
	final protected InitiumObject object;
	final protected CachedEntity entity;
	
	protected InitiumAspect(InitiumObject object)
	{
		this.object = object;
		this.entity = object.getEntity();
		this.db = object.getDB();
	}

	/**
	 * This method is called when the aspect class is instantiated. Use this to set
	 * default field values.
	 */
	protected abstract void initialize();
}
