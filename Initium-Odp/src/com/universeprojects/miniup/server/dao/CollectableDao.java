package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Collectable;

public class CollectableDao extends OdpDao<Collectable> {

	public CollectableDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public Collectable get(Key key) {
		Collectable o = null;
		try {
			CachedEntity oEntity = getDatastore().get(key);
			if (oEntity != null) {
				o = new Collectable(oEntity);
			}
		} catch (EntityNotFoundException e) {

		}
		return o;
	}
}
