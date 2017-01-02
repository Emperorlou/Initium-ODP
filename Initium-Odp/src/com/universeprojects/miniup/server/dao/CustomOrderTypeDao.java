package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.CustomOrderType;

public class CustomOrderTypeDao extends OdpDao<CustomOrderType> {

	public CustomOrderTypeDao(CachedDatastoreService datastore) {
		super(datastore);
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
			all.add(new CustomOrderType(entity));
		}
		return all;
	}

}
