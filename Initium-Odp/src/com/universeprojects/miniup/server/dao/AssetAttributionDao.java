package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.AssetAttribution;

public class AssetAttributionDao extends OdpDao<AssetAttribution> {

	private static final Logger log = Logger.getLogger("AssetAttributionDao");

	public AssetAttributionDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public AssetAttribution get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new AssetAttribution(entity);
	}

	@Override
	public List<AssetAttribution> findAll() {
		List<AssetAttribution> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(AssetAttribution.KIND)) {
			if (entity == null) {
				getLogger().warning("Null entity received from query");
				continue;
			}

			all.add(new AssetAttribution(entity));
		}
		return all;
	}

}
