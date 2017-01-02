package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.CustomOrder;
import com.universeprojects.miniup.server.exceptions.DaoException;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class CustomOrderDao extends OdpDao<CustomOrder> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public CustomOrderDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public CustomOrder get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new CustomOrder(entity);
	}

	@Override
	public List<CustomOrder> findAll() throws DaoException {
		return buildList(findAllCachedEntities(CustomOrder.KIND), CustomOrder.class);
	}

	@Override
	public List<CustomOrder> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		return buildList(getDatastore().get(keyList), CustomOrder.class);
	}

}
