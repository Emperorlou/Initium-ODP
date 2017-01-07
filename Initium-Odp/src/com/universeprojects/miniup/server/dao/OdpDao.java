package com.universeprojects.miniup.server.dao;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
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

	private static final String WRAP_METHOD = "wrap";

	private final CachedDatastoreService datastore;

	private final String kind;

	private final Class<T> odpDomainClass;

	public OdpDao(CachedDatastoreService datastore, String kind, Class<T> odpDomainClass) {
		assert datastore != null : "Datastore service cannot be null";
		assert kind != null : "Kind cannot be null";
		assert odpDomainClass != null : "OdpDomainClass cannot be null";
		this.datastore = datastore;
		this.kind = kind;
		this.odpDomainClass = odpDomainClass;
	}

	protected abstract Logger getLogger();

	protected CachedDatastoreService getDatastore() {
		return this.datastore;
	}
	
	protected String getKind() {
		return this.kind;
	}
	
	protected Class<T> getOdpDomainClass() {
		return this.odpDomainClass;
	}

	@SuppressWarnings("unchecked")
	protected T buildDomain(CachedEntity cachedEntity, Class<T> domainClass) throws DaoException {
		if (cachedEntity == null) {
			getLogger().warning("Null entity found");
			return null;
		}

		try {
			Method declaredMethod = domainClass.getDeclaredMethod(WRAP_METHOD, CachedEntity.class);
			T odpDomainEntity = (T) declaredMethod.invoke(cachedEntity);
			return odpDomainEntity;
		} catch (ReflectiveOperationException | IllegalArgumentException e) {
			throw new DaoException(String.format("%s is set up incorrectly", getOdpDomainClass().getName()), e);
		} catch (SecurityException e) {
			throw new DaoException(String.format("Unable to access declared method %s", WRAP_METHOD), e);
		}
	}

	protected List<T> buildOdpDomainList(List<CachedEntity> cachedEntities) throws DaoException {
		if (cachedEntities == null || cachedEntities.isEmpty()) {
			return Collections.emptyList();
		}

		List<T> odpDomainEntities = new ArrayList<>();
		for (CachedEntity entity : cachedEntities) {
				T domainEntity = buildDomain(entity, odpDomainClass);
				if (domainEntity != null) {
					odpDomainEntities.add(domainEntity);
				}
		}
		return odpDomainEntities;
	}

	public boolean save(T t) {
		assert t != null && t.getCachedEntity() != null : "Cannot save a null entity";
		getDatastore().put(t.getCachedEntity());
		return true;
	}

	public T get(Key key) throws DaoException  {
		T odpDomainEntity = null;

		try {
			CachedEntity cachedEntity = getDatastore().get(key);
			odpDomainEntity = buildDomain(cachedEntity, odpDomainClass);
		} catch (EntityNotFoundException e) {
			getLogger().fine(String.format("Non-existing key requested for %s", key));
		}

		return odpDomainEntity;
	}

	public List<T> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		List<CachedEntity> cachedEntities = getDatastore().get(keyList);
		List<T> odpDomainEntities = buildOdpDomainList(cachedEntities);
		return odpDomainEntities;
	}

	public List<T> findAll() throws DaoException {
		List<CachedEntity> cachedEntities = getDatastore().fetchAsList(kind, null, MAX_QUERY_RESULTS);
		List<T> odpDomainEntities = buildOdpDomainList(cachedEntities);
		return odpDomainEntities;
	}

}
