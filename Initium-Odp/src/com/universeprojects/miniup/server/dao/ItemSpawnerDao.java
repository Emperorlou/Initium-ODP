package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.ItemSpawner;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class ItemSpawnerDao extends OdpDao<ItemSpawner> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public ItemSpawnerDao(CachedDatastoreService datastore) {
		super(datastore, ItemSpawner.KIND, ItemSpawner.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
