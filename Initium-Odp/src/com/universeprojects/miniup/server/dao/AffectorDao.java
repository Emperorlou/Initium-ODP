package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.Affector;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class AffectorDao extends OdpDao<Affector> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public AffectorDao(CachedDatastoreService datastore) {
		super(datastore, Affector.KIND, Affector.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
