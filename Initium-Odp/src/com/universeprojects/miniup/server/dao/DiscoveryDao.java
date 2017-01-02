package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Discovery;

public class DiscoveryDao extends OdpDao<Discovery> {

	public DiscoveryDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public Discovery get(Key key) {
		Discovery o = null;
		try {
			CachedEntity oEntity = getDatastore().get(key);
			if (oEntity != null) {
				o = new Discovery(oEntity);
			}
		} catch (EntityNotFoundException e) {

		}
		return o;
	}
}
