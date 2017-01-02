package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.SaleItem;
import com.universeprojects.miniup.server.exceptions.DaoException;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class SaleItemDao extends OdpDao<SaleItem> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public SaleItemDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public SaleItem get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new SaleItem(entity);
	}

	@Override
	public List<SaleItem> findAll() throws DaoException {
		return buildList(findAllCachedEntities(SaleItem.KIND), SaleItem.class);
	}

	@Override
	public List<SaleItem> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		return buildList(getDatastore().get(keyList), SaleItem.class);
	}

}
