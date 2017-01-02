package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.AssetAttribution;

public class AssetAttributionDao extends OdpDao<AssetAttribution> {

	public AssetAttributionDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public AssetAttribution get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new AssetAttribution(entity);
	}
}
