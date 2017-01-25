package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.Collectable;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class CollectableDao extends OdpDao<Collectable> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public CollectableDao(CachedDatastoreService datastore) {
		super(datastore, Collectable.KIND, Collectable.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
