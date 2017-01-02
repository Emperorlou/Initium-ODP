package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Script;
import com.universeprojects.miniup.server.exceptions.DaoException;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class ScriptDao extends OdpDao<Script> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

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
	public List<Script> findAll() throws DaoException {
		return buildList(findAllCachedEntities(Script.KIND), Script.class);
	}

	@Override
	public List<Script> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		return buildList(getDatastore().get(keyList), Script.class);
	}

}
