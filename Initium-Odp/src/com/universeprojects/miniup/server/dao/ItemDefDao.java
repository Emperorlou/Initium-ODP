package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.ItemDef;

public class ItemDefDao extends OdpDao<ItemDef> {

	private static final Logger log = Logger.getLogger("ItemDefDao");

	public ItemDefDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
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
			if (entity == null) {
				getLogger().warning("Null entity received from query");
				continue;
			}

			all.add(new ItemDef(entity));
		}
		return all;
	}

}
