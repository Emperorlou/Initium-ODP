package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;

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

	@Override
	public List<ItemDef> findAll() {
		List<ItemDef> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(ItemDef.KIND)) {
			all.add(new ItemDef(entity));
		}
		return all;
	}

}
