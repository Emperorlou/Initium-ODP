package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.Discovery;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class DiscoveryDao extends OdpDao<Discovery> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public DiscoveryDao(CachedDatastoreService datastore) {
		super(datastore, Discovery.KIND, Discovery.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
