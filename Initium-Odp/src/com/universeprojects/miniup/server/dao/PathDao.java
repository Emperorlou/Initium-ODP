package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;

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

	@Override
	public List<Path> findAll() {
		List<Path> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(Path.KIND)) {
			all.add(new Path(entity));
		}
		return all;
	}

}
