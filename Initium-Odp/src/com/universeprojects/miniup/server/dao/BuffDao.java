package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Buff;

public class BuffDao extends OdpDao<Buff> {

	public BuffDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public Buff get(Key key) {
		Buff o = null;
		try {
			CachedEntity oEntity = getDatastore().get(key);
			if (oEntity != null) {
				o = new Buff(oEntity);
			}
		} catch (EntityNotFoundException e) {

		}
		return o;
	}
}
