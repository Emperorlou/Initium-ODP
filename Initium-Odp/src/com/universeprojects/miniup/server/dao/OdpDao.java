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

/**
 * Generic parent DAO for all OdpDomain objects
 * 
 * @author kyle-miller
 *
 * @param <T> - An OdpDomain type
 */
public abstract class OdpDao<T extends OdpDomain> {

	/** Maximum number of objects that should be returned from a findAll query. At the moment this cannot be overridden */
	public static final int MAX_QUERY_RESULTS = 1000;

	/** Method that should be on every OdpDomain instance that will be called to create OdpDomain objects from CachedEntities */
	private static final String WRAP_METHOD = "wrap";

	private final CachedDatastoreService datastore;

	private final String kind;

	private final Class<T> odpDomainClass;

	/**
	 * Constructor that should be called for every implementing class.
	 * 
	 * @param datastore - Should be passed in by the caller instantiating the DAO.
	 * @param kind - The OdpDomain type that will be used in the current OdpDao instance.
	 * @param odpDomainClass - The class type of the OdpDomain instance used in this OdpDao instance. 
	 */
	protected OdpDao(CachedDatastoreService datastore, String kind, Class<T> odpDomainClass) {
		assert datastore != null : "Datastore service cannot be null";
		assert kind != null : "Kind cannot be null";
		assert odpDomainClass != null : "OdpDomainClass cannot be null";

		this.datastore = datastore;
		this.kind = kind;
		this.odpDomainClass = odpDomainClass;
	}

	/**
	 * Each instance of this class will implement it's own Logger and a method to retrieve it.
	 * 
	 * @return
	 */
	protected abstract Logger getLogger();

	/**
	 * Get the current DAO instance's CachedDatastoreService.
	 * 
	 * @return
	 */
	protected CachedDatastoreService getDatastore() {
		return this.datastore;
	}

	/**
	 * Get the current DAO instance's kind.
	 * 
	 * @return
	 */
	protected String getKind() {
		return this.kind;
	}

	/**
	 * Get the current DAO instance's OdpDomain class instance.
	 * 
	 * @return
	 */
	protected Class<T> getOdpDomainClass() {
		return this.odpDomainClass;
	}

	/**
	 * Builds a single OdpDomain class from a CachedEntity
	 * 
	 * @param cachedEntity
	 * @return
	 * @throws DaoException - If an exception occurs from a query or an incorrectly set up domain/DAO
	 */
	@SuppressWarnings("unchecked")
	protected T buildDomain(CachedEntity cachedEntity) throws DaoException {
		if (cachedEntity == null) {
			getLogger().warning("Null entity found");
			return null;
		}

		try {
			Method declaredMethod = getOdpDomainClass().getDeclaredMethod(WRAP_METHOD, CachedEntity.class);
			T odpDomainEntity = (T) declaredMethod.invoke(getOdpDomainClass(), cachedEntity);
			return odpDomainEntity;
		} catch (ReflectiveOperationException | IllegalArgumentException e) {
			throw new DaoException(String.format("%s is set up incorrectly", getOdpDomainClass().getName()), e);
		} catch (SecurityException e) {
			throw new DaoException(String.format("Unable to access declared method %s", WRAP_METHOD), e);
		}
	}

	/**
	 * Builds a list of OdpDomain class objects from a list of CachedEntities
	 * 
	 * @param cachedEntities
	 * @return
	 * @throws DaoException - If an exception occurs from a query or an incorrectly set up domain/DAO
	 */
	protected List<T> buildOdpDomainList(List<CachedEntity> cachedEntities) throws DaoException {
		if (cachedEntities == null || cachedEntities.isEmpty()) {
			return Collections.emptyList();
		}

		List<T> odpDomainEntities = new ArrayList<>();
		for (CachedEntity entity : cachedEntities) {
				T domainEntity = buildDomain(entity);
				if (domainEntity != null) {
					odpDomainEntities.add(domainEntity);
				}
		}
		return odpDomainEntities;
	}

	/**
	 * Saves an OdpDomain object.
	 * 
	 * @param t
	 * @return true on success
	 */
	public boolean save(T t) {
		assert t != null && t.getCachedEntity() != null : "Cannot save a null entity";
		getDatastore().put(t.getCachedEntity());
		return true;
	}

	/**
	 * Attempts to retrieve a domain object by the given key.
	 * 
	 * @param key
	 * @return OdpDomain object or null if key doesn't exist
	 * @throws DaoException - On exception performing query or transforming returned type to the DAO's domain type
	 */
	public T get(Key key) throws DaoException  {
		T odpDomainEntity = null;

		try {
			CachedEntity cachedEntity = getDatastore().get(key);
			odpDomainEntity = buildDomain(cachedEntity);
		} catch (EntityNotFoundException e) {
			getLogger().fine(String.format("Non-existing key requested for %s", key));
		}

		return odpDomainEntity;
	}

	/**
	 * Attempts to retrieve a list of domain objects matching the keys in the given list. Any given keys that do not match domain objects will not be included in the response.
	 * 
	 * @param keyList
	 * @return List of OdpDomain objects.
	 * @throws DaoException - On exception performing query or transforming returned type to the DAO's domain type
	 */
	public List<T> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		List<CachedEntity> cachedEntities = getDatastore().get(keyList);
		List<T> odpDomainEntities = buildOdpDomainList(cachedEntities);
		return odpDomainEntities;
	}

	/**
	 * Attempts to retrieve a list of domain objects by kind.
	 * 
	 * @return List of OdpDomain objects. Maximum number of items is {@value #MAX_QUERY_RESULTS}
	 * @throws DaoException - On exception performing query or transforming returned type to the DAO's domain type
	 */
	public List<T> findAll() throws DaoException {
		List<CachedEntity> cachedEntities = getDatastore().fetchAsList(kind, null, MAX_QUERY_RESULTS);
		List<T> odpDomainEntities = buildOdpDomainList(cachedEntities);
		return odpDomainEntities;
	}

}
