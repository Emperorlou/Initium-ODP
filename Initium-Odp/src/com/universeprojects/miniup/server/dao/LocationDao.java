package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Location;
import com.universeprojects.miniup.server.exceptions.DaoException;

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
	public List<Location> findAll() throws DaoException {
		return buildList(findAllCachedEntities(Location.KIND), Location.class);
	}

	@Override
	public List<Location> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		return buildList(getDatastore().get(keyList), Location.class);
	}

}
