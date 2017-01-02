package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.CollectableSpawner;

public class CollectableSpawnerDao extends OdpDao<CollectableSpawner> {

	public CollectableSpawnerDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public CollectableSpawner get(Key key) {
		CollectableSpawner o = null;
		try {
			CachedEntity oEntity = getDatastore().get(key);
			if (oEntity != null) {
				o = new CollectableSpawner(oEntity);
			}
		} catch (EntityNotFoundException e) {

		}
		return o;
	}
}
