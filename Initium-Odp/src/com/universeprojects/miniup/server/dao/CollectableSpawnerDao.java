package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.CollectableSpawner;
import com.universeprojects.miniup.server.exceptions.DaoException;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class CollectableSpawnerDao extends OdpDao<CollectableSpawner> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public CollectableSpawnerDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public CollectableSpawner get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new CollectableSpawner(entity);
	}

	@Override
	public List<CollectableSpawner> findAll() throws DaoException {
		return buildList(findAllCachedEntities(CollectableSpawner.KIND), CollectableSpawner.class);
	}

	@Override
	public List<CollectableSpawner> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		return buildList(getDatastore().get(keyList), CollectableSpawner.class);
	}

}
