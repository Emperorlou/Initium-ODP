package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
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

	protected CachedEntity getCachedEntity(Key key) {
		try {
			return getDatastore().get(key);
		} catch (EntityNotFoundException e) {
			return null;
		}
	}

	public abstract T get(Key key);
}
