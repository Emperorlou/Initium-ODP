package com.universeprojects.miniup.server.dbentities;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.QueryHelper;
import com.universeprojects.miniup.server.ODPDBAccess;

public abstract class InitiumEntityBase
{
	protected CachedEntity entity;
	protected ODPDBAccess db;
	protected CachedDatastoreService ds;
	protected QueryHelper query;
	
	public InitiumEntityBase(ODPDBAccess db, CachedEntity entity)
	{
		if (entity.getKind().equals(getKind())==false)
			throw new IllegalArgumentException("The entity given is not a "+getKind()+" kind but a "+entity.getKind()+".");
		
		this.db = db;
		this.entity = entity;
		this.ds = db.getDB();
		this.query = new QueryHelper(ds);
	}
	
	protected abstract String getKind();
	
	public Key getKey()
	{
		return entity.getKey();
	}
	
	public String getUrlSafeKey()
	{
		return KeyFactory.keyToString(entity.getKey());
	}
	
	public CachedEntity getRawEntity()
	{
		return entity;
	}
	
	
	
	
}
