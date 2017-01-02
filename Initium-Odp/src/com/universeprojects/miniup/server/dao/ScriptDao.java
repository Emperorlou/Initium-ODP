package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Script;

public class ScriptDao extends OdpDao<Script> {

	public ScriptDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public Script get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new Script(entity);
	}

	@Override
	public List<Script> findAll() {
		List<Script> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(Script.KIND)) {
			all.add(new Script(entity));
		}
		return all;
	}

}
