package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Location;

public class LocationDao extends OdpDao<Location> {

	public LocationDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public Location get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new Location(entity);
	}

	@Override
	public List<Location> findAll() {
		List<Location> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(Location.KIND)) {
			all.add(new Location(entity));
		}
		return all;
	}

}
