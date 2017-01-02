package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Location;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class LocationDao extends OdpDao<Location> {

	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public LocationDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public Location get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new Location(entity);
	}

	@Override
	public List<Location> findAll() {
		List<Location> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(Location.KIND)) {
			if (entity == null) {
				getLogger().warning("Null entity received from query");
				continue;
			}

			all.add(new Location(entity));
		}
		return all;
	}

}
