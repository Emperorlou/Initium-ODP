package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Quotes;

public class QuotesDao extends OdpDao<Quotes> {

	public QuotesDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public Quotes get(Key key) {
		Quotes o = null;
		try {
			CachedEntity oEntity = getDatastore().get(key);
			if (oEntity != null) {
				o = new Quotes(oEntity);
			}
		} catch (EntityNotFoundException e) {

		}
		return o;
	}
}
