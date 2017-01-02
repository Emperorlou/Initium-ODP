package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Territory;

public class TerritoryDao extends OdpDao<Territory> {

	public TerritoryDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public Territory get(Key key) {
		Territory o = null;
		try {
			CachedEntity oEntity = getDatastore().get(key);
			if (oEntity != null) {
				o = new Territory(oEntity);
			}
		} catch (EntityNotFoundException e) {

		}
		return o;
	}
}
