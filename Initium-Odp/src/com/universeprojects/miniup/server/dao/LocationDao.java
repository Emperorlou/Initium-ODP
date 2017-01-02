package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
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
		Location o = null;
		try {
			CachedEntity oEntity = getDatastore().get(key);
			if (oEntity != null) {
				o = new Location(oEntity);
			}
		} catch (EntityNotFoundException e) {

		}
		return o;
	}
}
