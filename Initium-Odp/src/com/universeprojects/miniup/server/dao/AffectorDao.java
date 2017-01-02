package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;

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

	@Override
	public List<Affector> findAll() {
		List<Affector> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(Affector.KIND)) {
			all.add(new Affector(entity));
		}
		return all;
	}

}
