package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.SaleItem;

public class SaleItemDao extends OdpDao<SaleItem> {

	public SaleItemDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public SaleItem get(Key key) {
		SaleItem o = null;
		try {
			CachedEntity oEntity = getDatastore().get(key);
			if (oEntity != null) {
				o = new SaleItem(oEntity);
			}
		} catch (EntityNotFoundException e) {

		}
		return o;
	}
}
