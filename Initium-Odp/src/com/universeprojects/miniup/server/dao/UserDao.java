package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
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
		User o = null;
		try {
			CachedEntity oEntity = getDatastore().get(key);
			if (oEntity != null) {
				o = new User(oEntity);
			}
		} catch (EntityNotFoundException e) {

		}
		return o;
	}
}
