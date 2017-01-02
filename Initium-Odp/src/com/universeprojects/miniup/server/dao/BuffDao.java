package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Buff;

public class BuffDao extends OdpDao<Buff> {

	public BuffDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public Buff get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new Buff(entity);
	}
}
