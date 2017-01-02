package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Friend;

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
	public List<Friend> findAll() {
		List<Friend> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(Friend.KIND)) {
			if (entity == null) {
				getLogger().warning("Null entity received from query");
				continue;
			}

			all.add(new Friend(entity));
		}
		return all;
	}

}
