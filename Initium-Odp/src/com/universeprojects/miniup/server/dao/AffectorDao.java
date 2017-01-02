package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Affector;

public class AffectorDao extends OdpDao<Affector> {

	public AffectorDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public Affector get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new Affector(entity);
	}
}
