package com.universeprojects.miniup.server.dao;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.OdpDomain;
import com.universeprojects.miniup.server.exceptions.DaoException;

public abstract class OdpDao<T extends OdpDomain> {

	public static final int MAX_QUERY_RESULTS = 1000;

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
		return getDatastore().fetchAsList(kind, null, MAX_QUERY_RESULTS);
	}

	protected List<T> buildList(List<CachedEntity> cachedEntities, Class<T> domainClass) throws DaoException {
		try {
			List<T> all = new ArrayList<>();
			if (cachedEntities != null) {
				Constructor<T> constructor = domainClass.getConstructor(CachedEntity.class);
				for (CachedEntity entity : cachedEntities) {
					if (entity == null) {
						getLogger().warning("Null entity received from query");
						continue;
					}
					all.add(constructor.newInstance(entity));
				}
			}
			return all;
		} catch (ReflectiveOperationException e) {
			// Each DAO should have tests ensuring this doesn't happen
			throw new DaoException(String.format("%s is set up incorrectly", getClass().getName()), e);
		}
	}

	protected abstract Logger getLogger();

	public abstract T get(Key key);

	public abstract List<T> get(List<Key> keyList) throws DaoException;

	public abstract List<T> findAll() throws DaoException;
}
