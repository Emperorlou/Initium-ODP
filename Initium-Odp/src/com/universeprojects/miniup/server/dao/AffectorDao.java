package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Affector;

public class AffectorDao extends OdpDao<Affector> {

	public AffectorDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public Affector get(Key key) {
		Affector o = null;
		try {
			CachedEntity oEntity = getDatastore().get(key);
			if (oEntity != null) {
				o = new Affector(oEntity);
			}
		} catch (EntityNotFoundException e) {

		}
		return o;
	}
}
