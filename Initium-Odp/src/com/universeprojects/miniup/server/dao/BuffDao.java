package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Buff;

public class BuffDao extends OdpDao<Buff> {

	public BuffDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public Buff get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new Buff(entity);
	}

	@Override
	public List<Buff> findAll() {
		List<Buff> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(Buff.KIND)) {
			all.add(new Buff(entity));
		}
		return all;
	}

}
