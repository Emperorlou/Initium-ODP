package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.CustomOrderType;

public class CustomOrderTypeDao extends OdpDao<CustomOrderType> {

	public CustomOrderTypeDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public CustomOrderType get(Key key) {
		CustomOrderType o = null;
		try {
			CachedEntity oEntity = getDatastore().get(key);
			if (oEntity != null) {
				o = new CustomOrderType(oEntity);
			}
		} catch (EntityNotFoundException e) {

		}
		return o;
	}
}
