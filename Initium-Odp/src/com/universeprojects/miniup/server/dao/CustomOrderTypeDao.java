package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.CustomOrderType;

public class CustomOrderTypeDao extends OdpDao<CustomOrderType> {

	private static final Logger log = Logger.getLogger("CustomOrderTypeDao");

	public CustomOrderTypeDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public CustomOrderType get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new CustomOrderType(entity);
	}

	@Override
	public List<CustomOrderType> findAll() {
		List<CustomOrderType> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(CustomOrderType.KIND)) {
			if (entity == null) {
				getLogger().warning("Null entity received from query");
				continue;
			}

			all.add(new CustomOrderType(entity));
		}
		return all;
	}

}
