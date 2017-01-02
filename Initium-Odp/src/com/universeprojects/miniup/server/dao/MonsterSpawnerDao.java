package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.MonsterSpawner;

public class MonsterSpawnerDao extends OdpDao<MonsterSpawner> {

	public MonsterSpawnerDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public MonsterSpawner get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new MonsterSpawner(entity);
	}

	@Override
	public List<MonsterSpawner> findAll() {
		List<MonsterSpawner> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(MonsterSpawner.KIND)) {
			all.add(new MonsterSpawner(entity));
		}
		return all;
	}

}
