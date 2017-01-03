package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.User;
import com.universeprojects.miniup.server.exceptions.DaoException;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class UserDao extends OdpDao<User> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

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
	public List<User> findAll() throws DaoException {
		return buildList(findAllCachedEntities(User.KIND), User.class);
	}

	@Override
	public List<User> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		return buildList(getDatastore().get(keyList), User.class);
	}

}