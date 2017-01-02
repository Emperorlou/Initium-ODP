package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.ItemSpawner;

public class ItemSpawnerDao extends OdpDao<ItemSpawner> {

	private static final Logger log = Logger.getLogger("ItemSpawnerDao");

	public ItemSpawnerDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public ItemSpawner get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new ItemSpawner(entity);
	}

	@Override
	public List<ItemSpawner> findAll() {
		List<ItemSpawner> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(ItemSpawner.KIND)) {
			if (entity == null) {
				getLogger().warning("Null entity received from query");
				continue;
			}

			all.add(new ItemSpawner(entity));
		}
		return all;
	}

}
