package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.Location;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class LocationDao extends OdpDao<Location> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public LocationDao(CachedDatastoreService datastore) {
		super(datastore, Location.KIND, Location.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
