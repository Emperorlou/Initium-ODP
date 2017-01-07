package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.Territory;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class TerritoryDao extends OdpDao<Territory> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public TerritoryDao(CachedDatastoreService datastore) {
		super(datastore, Territory.KIND, Territory.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
