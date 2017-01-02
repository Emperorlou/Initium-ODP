package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Item;

public class ItemDao extends OdpDao<Item> {

	public ItemDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public Item get(Key key) {
		Item o = null;
		try {
			CachedEntity oEntity = getDatastore().get(key);
			if (oEntity != null) {
				o = new Item(oEntity);
			}
		} catch (EntityNotFoundException e) {

		}
		return o;
	}
}
