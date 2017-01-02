package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Path;

public class PathDao extends OdpDao<Path> {

	public PathDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public Path get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new Path(entity);
	}
}
