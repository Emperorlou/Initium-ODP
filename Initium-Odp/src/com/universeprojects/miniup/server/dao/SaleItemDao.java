package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.SaleItem;

public class SaleItemDao extends OdpDao<SaleItem> {

	private static final Logger log = Logger.getLogger("SaleItemDao");

	public SaleItemDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public SaleItem get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new SaleItem(entity);
	}

	@Override
	public List<SaleItem> findAll() {
		List<SaleItem> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(SaleItem.KIND)) {
			if (entity == null) {
				getLogger().warning("Null entity received from query");
				continue;
			}

			all.add(new SaleItem(entity));
		}
		return all;
	}

}
