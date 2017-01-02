package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.User;

public class UserDao extends OdpDao<User> {

	private static final Logger log = Logger.getLogger("UserDao");

	public UserDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
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
			if (entity == null) {
				getLogger().warning("Null entity received from query");
				continue;
			}

			all.add(new User(entity));
		}
		return all;
	}

}
