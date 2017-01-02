package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.CustomOrder;

public class CustomOrderDao extends OdpDao<CustomOrder> {

	public CustomOrderDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public CustomOrder get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new CustomOrder(entity);
	}
}
