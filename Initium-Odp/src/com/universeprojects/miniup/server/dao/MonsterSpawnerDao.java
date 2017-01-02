package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.MonsterSpawner;

public class MonsterSpawnerDao extends OdpDao<MonsterSpawner> {

	public MonsterSpawnerDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public MonsterSpawner get(Key key) {
		MonsterSpawner o = null;
		try {
			CachedEntity oEntity = getDatastore().get(key);
			if (oEntity != null) {
				o = new MonsterSpawner(oEntity);
			}
		} catch (EntityNotFoundException e) {

		}
		return o;
	}
}
