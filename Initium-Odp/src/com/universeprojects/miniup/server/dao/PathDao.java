package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
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
		Path o = null;
		try {
			CachedEntity oEntity = getDatastore().get(key);
			if (oEntity != null) {
				o = new Path(oEntity);
			}
		} catch (EntityNotFoundException e) {

		}
		return o;
	}
}
