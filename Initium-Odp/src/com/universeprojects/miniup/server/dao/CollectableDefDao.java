package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.CollectableDef;

public class CollectableDefDao extends OdpDao<CollectableDef> {

	private static final Logger log = Logger.getLogger("CollectableDefDao");

	public CollectableDefDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public CollectableDef get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new CollectableDef(entity);
	}

	@Override
	public List<CollectableDef> findAll() {
		List<CollectableDef> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(CollectableDef.KIND)) {
			if (entity == null) {
				getLogger().warning("Null entity received from query");
				continue;
			}

			all.add(new CollectableDef(entity));
		}
		return all;
	}

}
