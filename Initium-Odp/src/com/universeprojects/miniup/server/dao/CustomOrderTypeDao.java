package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.CustomOrderType;
import com.universeprojects.miniup.server.exceptions.DaoException;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class CustomOrderTypeDao extends OdpDao<CustomOrderType> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public CustomOrderTypeDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public CustomOrderType get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new CustomOrderType(entity);
	}

	@Override
	public List<CustomOrderType> findAll() throws DaoException {
		return buildList(findAllCachedEntities(CustomOrderType.KIND), CustomOrderType.class);
	}

	@Override
	public List<CustomOrderType> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		return buildList(getDatastore().get(keyList), CustomOrderType.class);
	}

}
