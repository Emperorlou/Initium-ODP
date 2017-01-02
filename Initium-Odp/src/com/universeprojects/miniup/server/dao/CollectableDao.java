package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Collectable;

public class CollectableDao extends OdpDao<Collectable> {

	private static final Logger log = Logger.getLogger("CollectableDao");

	public CollectableDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public Collectable get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new Collectable(entity);
	}

	@Override
	public List<Collectable> findAll() {
		List<Collectable> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(Collectable.KIND)) {
			if (entity == null) {
				getLogger().warning("Null entity received from query");
				continue;
			}

			all.add(new Collectable(entity));
		}
		return all;
	}

}
