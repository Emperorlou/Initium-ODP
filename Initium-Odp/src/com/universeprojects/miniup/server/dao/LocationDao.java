package com.universeprojects.miniup.server.dao;

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
}
