package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Tag;

public class TagDao extends OdpDao<Tag> {

	public TagDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public Tag get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new Tag(entity);
	}

	@Override
	public List<Tag> findAll() {
		List<Tag> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(Tag.KIND)) {
			all.add(new Tag(entity));
		}
		return all;
	}

}
