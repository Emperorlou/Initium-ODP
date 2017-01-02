package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Affector;
import com.universeprojects.miniup.server.exceptions.DaoException;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class AffectorDao extends OdpDao<Affector> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public AffectorDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public Affector get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new Affector(entity);
	}

	@Override
	public List<Affector> findAll() throws DaoException {
		return buildList(findAllCachedEntities(Affector.KIND), Affector.class);
	}

	@Override
	public List<Affector> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		return buildList(getDatastore().get(keyList), Affector.class);
	}

}
