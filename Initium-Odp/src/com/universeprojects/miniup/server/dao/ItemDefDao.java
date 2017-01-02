package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.ItemDef;

public class ItemDefDao extends OdpDao<ItemDef> {

	public ItemDefDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public ItemDef get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new ItemDef(entity);
	}
}
