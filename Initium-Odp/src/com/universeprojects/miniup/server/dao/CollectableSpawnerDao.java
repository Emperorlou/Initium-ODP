package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.CollectableSpawner;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class CollectableSpawnerDao extends OdpDao<CollectableSpawner> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public CollectableSpawnerDao(CachedDatastoreService datastore) {
		super(datastore, CollectableSpawner.KIND, CollectableSpawner.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
