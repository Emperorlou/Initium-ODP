package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.MonsterSpawner;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class MonsterSpawnerDao extends OdpDao<MonsterSpawner> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public MonsterSpawnerDao(CachedDatastoreService datastore) {
		super(datastore, MonsterSpawner.KIND, MonsterSpawner.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
