package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.CollectableDef;

public class CollectableDefDao extends OdpDao<CollectableDef> {

	public CollectableDefDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public CollectableDef get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new CollectableDef(entity);
	}
}
