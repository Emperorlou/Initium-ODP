package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.MonsterSpawner;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class MonsterSpawnerDao extends OdpDao<MonsterSpawner> {

	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public MonsterSpawnerDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public MonsterSpawner get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new MonsterSpawner(entity);
	}

	@Override
	public List<MonsterSpawner> findAll() {
		List<MonsterSpawner> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(MonsterSpawner.KIND)) {
			if (entity == null) {
				getLogger().warning("Null entity received from query");
				continue;
			}

			all.add(new MonsterSpawner(entity));
		}
		return all;
	}

}
