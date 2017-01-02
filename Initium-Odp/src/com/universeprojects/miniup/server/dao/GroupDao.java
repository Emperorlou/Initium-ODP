package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Group;

public class GroupDao extends OdpDao<Group> {

	public GroupDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public Group get(Key key) {
		Group o = null;
		try {
			CachedEntity oEntity = getDatastore().get(key);
			if (oEntity != null) {
				o = new Group(oEntity);
			}
		} catch (EntityNotFoundException e) {

		}
		return o;
	}
}
