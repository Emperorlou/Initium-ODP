package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.CollectableSpawner;

public class CollectableSpawnerDao extends OdpDao<CollectableSpawner> {

	public CollectableSpawnerDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public CollectableSpawner get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new CollectableSpawner(entity);
	}

	@Override
	public List<CollectableSpawner> findAll() {
		List<CollectableSpawner> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(CollectableSpawner.KIND)) {
			all.add(new CollectableSpawner(entity));
		}
		return all;
	}

}
