package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.Path;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class PathDao extends OdpDao<Path> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public PathDao(CachedDatastoreService datastore) {
		super(datastore, Path.KIND, Path.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
