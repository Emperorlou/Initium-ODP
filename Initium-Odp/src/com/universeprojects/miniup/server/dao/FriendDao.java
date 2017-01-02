package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Friend;

public class FriendDao extends OdpDao<Friend> {

	public FriendDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public Friend get(Key key) {
		Friend o = null;
		try {
			CachedEntity oEntity = getDatastore().get(key);
			if (oEntity != null) {
				o = new Friend(oEntity);
			}
		} catch (EntityNotFoundException e) {

		}
		return o;
	}
}
