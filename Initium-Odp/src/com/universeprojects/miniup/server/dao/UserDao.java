package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.User;

public class UserDao extends OdpDao<User> {

	public UserDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public User get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new User(entity);
	}
}
