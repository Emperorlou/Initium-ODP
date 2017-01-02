package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.ItemSpawner;

public class ItemSpawnerDao extends OdpDao<ItemSpawner> {

	public ItemSpawnerDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public ItemSpawner get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new ItemSpawner(entity);
	}
}
