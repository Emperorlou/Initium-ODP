package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Tag;

public class TagDao extends OdpDao<Tag> {

	public TagDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public Tag get(Key key) {
		Tag o = null;
		try {
			CachedEntity oEntity = getDatastore().get(key);
			if (oEntity != null) {
				o = new Tag(oEntity);
			}
		} catch (EntityNotFoundException e) {

		}
		return o;
	}
}
