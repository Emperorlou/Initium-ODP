package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.CollectableDef;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class CollectableDefDao extends OdpDao<CollectableDef> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public CollectableDefDao(CachedDatastoreService datastore) {
		super(datastore, CollectableDef.KIND, CollectableDef.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
