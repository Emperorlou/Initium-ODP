package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Territory;

public class TerritoryDao extends OdpDao<Territory> {

	private static final Logger log = Logger.getLogger("TerritoryDao");

	public TerritoryDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public Territory get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new Territory(entity);
	}

	@Override
	public List<Territory> findAll() {
		List<Territory> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(Territory.KIND)) {
			if (entity == null) {
				getLogger().warning("Null entity received from query");
				continue;
			}

			all.add(new Territory(entity));
		}
		return all;
	}

}
