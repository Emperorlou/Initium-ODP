package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Territory;
import com.universeprojects.miniup.server.exceptions.DaoException;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class TerritoryDao extends OdpDao<Territory> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public TerritoryDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public Territory get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new Territory(entity);
	}

	@Override
	public List<Territory> findAll() throws DaoException {
		return buildList(findAllCachedEntities(Territory.KIND), Territory.class);
	}

	@Override
	public List<Territory> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		return buildList(getDatastore().get(keyList), Territory.class);
	}

}
