package com.universeprojects.miniup.server.dao;

import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.OdpDomain;

public abstract class OdpDao<T extends OdpDomain> {

	private CachedDatastoreService datastore;

	public OdpDao(CachedDatastoreService datastore) {
		assert datastore != null : "Datastore service cannot be null";
		this.datastore = datastore;
	}

	protected CachedDatastoreService getDatastore() {
		return this.datastore;
	}

	public boolean save(T o) {
		assert o != null && o.getCachedEntity() != null : "Cannot save a null entity";
		getDatastore().put(o.getCachedEntity());
		return true;
	}

	protected CachedEntity getCachedEntity(Key key) {
		try {
			CachedEntity cachedEntity = getDatastore().get(key);
			if (cachedEntity == null) {
				getLogger().warning("Retrieved a CachedEntity key that has a null value");
			} else if (cachedEntity.getEntity() == null) {
				cachedEntity = null;
				getLogger().warning("Retrieved an Entity key that has a null value");
			}
			return cachedEntity;
		} catch (EntityNotFoundException e) {
			return null;
		}
	}

	protected List<CachedEntity> findAllCachedEntities(String kind) {
		return getDatastore().fetchAsList(kind, null, Integer.MAX_VALUE);
	}

	protected abstract Logger getLogger();

	public abstract T get(Key key);

	public abstract List<T> findAll();
}
