package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Friend;
import com.universeprojects.miniup.server.exceptions.DaoException;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class FriendDao extends OdpDao<Friend> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public FriendDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public Friend get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new Friend(entity);
	}

	@Override
	public List<Friend> findAll() throws DaoException {
		return buildList(findAllCachedEntities(Friend.KIND), Friend.class);
	}

	@Override
	public List<Friend> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		return buildList(getDatastore().get(keyList), Friend.class);
	}

}