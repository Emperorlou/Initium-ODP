package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;

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

	@Override
	public List<Discovery> findAll() {
		List<Discovery> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(Discovery.KIND)) {
			all.add(new Discovery(entity));
		}
		return all;
	}

}
