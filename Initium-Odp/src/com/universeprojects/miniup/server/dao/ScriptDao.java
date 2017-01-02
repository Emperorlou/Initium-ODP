package com.universeprojects.miniup.server.dao;

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
}
