package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Buff;
import com.universeprojects.miniup.server.exceptions.DaoException;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class BuffDao extends OdpDao<Buff> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public BuffDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public Buff get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new Buff(entity);
	}

	@Override
	public List<Buff> findAll() throws DaoException {
		return buildList(findAllCachedEntities(Buff.KIND), Buff.class);
	}

	@Override
	public List<Buff> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		return buildList(getDatastore().get(keyList), Buff.class);
	}

}
