package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
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
		AssetAttribution o = null;
		try {
			CachedEntity oEntity = getDatastore().get(key);
			if (oEntity != null) {
				o = new AssetAttribution(oEntity);
			}
		} catch (EntityNotFoundException e) {

		}
		return o;
	}
}
