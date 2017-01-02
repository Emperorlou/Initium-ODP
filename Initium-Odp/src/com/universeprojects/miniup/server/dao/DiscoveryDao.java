package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Discovery;

public class DiscoveryDao extends OdpDao<Discovery> {

	public DiscoveryDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public Discovery get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new Discovery(entity);
	}
}
