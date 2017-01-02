package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.MonsterSpawner;
import com.universeprojects.miniup.server.exceptions.DaoException;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class MonsterSpawnerDao extends OdpDao<MonsterSpawner> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public MonsterSpawnerDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public MonsterSpawner get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new MonsterSpawner(entity);
	}

	@Override
	public List<MonsterSpawner> findAll() throws DaoException {
		return buildList(findAllCachedEntities(MonsterSpawner.KIND), MonsterSpawner.class);
	}

	@Override
	public List<MonsterSpawner> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		return buildList(getDatastore().get(keyList), MonsterSpawner.class);
	}

}
