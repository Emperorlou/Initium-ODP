package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.BuffDef;

public class BuffDefDao extends OdpDao<BuffDef> {

	public BuffDefDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public BuffDef get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new BuffDef(entity);
	}
}
