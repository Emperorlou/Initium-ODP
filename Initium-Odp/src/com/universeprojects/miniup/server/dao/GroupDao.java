package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;

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
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new Group(entity);
	}

	@Override
	public List<Group> findAll() {
		List<Group> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(Group.KIND)) {
			all.add(new Group(entity));
		}
		return all;
	}

}
