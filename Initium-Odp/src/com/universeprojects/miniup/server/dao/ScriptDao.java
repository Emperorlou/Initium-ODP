package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Script;

public class ScriptDao extends OdpDao<Script> {

	private static final Logger log = Logger.getLogger("ScriptDao");

	public ScriptDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
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
			if (entity == null) {
				getLogger().warning("Null entity received from query");
				continue;
			}

			all.add(new Script(entity));
		}
		return all;
	}

}
