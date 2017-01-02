package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.ItemDef;

public class ItemDefDao extends OdpDao<ItemDef> {

	public ItemDefDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public ItemDef get(Key key) {
		ItemDef o = null;
		try {
			CachedEntity oEntity = getDatastore().get(key);
			if (oEntity != null) {
				o = new ItemDef(oEntity);
			}
		} catch (EntityNotFoundException e) {

		}
		return o;
	}
}
