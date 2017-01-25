package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.Schema;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class SchemaDao extends OdpDao<Schema> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public SchemaDao(CachedDatastoreService datastore) {
		super(datastore, Schema.KIND, Schema.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}
}
