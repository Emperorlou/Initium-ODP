package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.CollectableDef;

public class CollectableDefDao extends OdpDao<CollectableDef> {

	public CollectableDefDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public CollectableDef get(Key key) {
		CollectableDef o = null;
		try {
			CachedEntity oEntity = getDatastore().get(key);
			if (oEntity != null) {
				o = new CollectableDef(oEntity);
			}
		} catch (EntityNotFoundException e) {

		}
		return o;
	}
}
