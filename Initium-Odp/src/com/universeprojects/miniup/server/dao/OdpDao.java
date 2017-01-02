package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.OdpDomain;

public abstract class OdpDao<T extends OdpDomain> {

	private CachedDatastoreService datastore;

	public OdpDao(CachedDatastoreService datastore) {
		assert datastore != null : "DatastoreService cannot be null";
		this.datastore = datastore;
	}

	protected CachedDatastoreService getDatastore() {
		return this.datastore;
	}

	public boolean save(T o) {
		datastore.put(o.getCachedEntity());
		return true;
	}

	public abstract T get(Key key);
}
