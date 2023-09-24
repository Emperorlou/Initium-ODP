package com.universeprojects.miniup.server.services;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.QueryHelper;
import com.universeprojects.miniup.server.ODPDBAccess;

public abstract class Service
{
	final protected QueryHelper query;
	final protected ODPDBAccess db;
	final protected CachedDatastoreService ds; 
	
	public Service(ODPDBAccess db)
	{
		this.db = db;
		this.ds = db.getDB();
		this.query = new QueryHelper(db.getDB());
	}
}
