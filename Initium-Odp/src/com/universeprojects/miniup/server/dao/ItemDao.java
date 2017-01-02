package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Item;

public class ItemDao extends OdpDao<Item> {

	private static final Logger log = Logger.getLogger("ItemDao");

	public ItemDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
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
			if (entity == null) {
				getLogger().warning("Null entity received from query");
				continue;
			}

			all.add(new Item(entity));
		}
		return all;
	}

}
