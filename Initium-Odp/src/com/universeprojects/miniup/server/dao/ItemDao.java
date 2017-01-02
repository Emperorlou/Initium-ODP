package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Item;

public class ItemDao extends OdpDao<Item> {

	public ItemDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public Item get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new Item(entity);
	}

	@Override
	public List<Item> findAll() {
		List<Item> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(Item.KIND)) {
			all.add(new Item(entity));
		}
		return all;
	}

}
