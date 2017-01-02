package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.CollectableDef;
import com.universeprojects.miniup.server.exceptions.DaoException;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class CollectableDefDao extends OdpDao<CollectableDef> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public CollectableDefDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public CollectableDef get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new CollectableDef(entity);
	}

	@Override
	public List<CollectableDef> findAll() throws DaoException {
		return buildList(findAllCachedEntities(CollectableDef.KIND), CollectableDef.class);
	}

	@Override
	public List<CollectableDef> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		return buildList(getDatastore().get(keyList), CollectableDef.class);
	}

}
