package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;

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

	@Override
	public List<User> findAll() {
		List<User> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(User.KIND)) {
			all.add(new User(entity));
		}
		return all;
	}

}
