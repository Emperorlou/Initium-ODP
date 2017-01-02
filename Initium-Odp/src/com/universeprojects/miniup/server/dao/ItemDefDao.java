package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.ItemDef;
import com.universeprojects.miniup.server.exceptions.DaoException;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class ItemDefDao extends OdpDao<ItemDef> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public ItemDefDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public ItemDef get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new ItemDef(entity);
	}

	@Override
	public List<ItemDef> findAll() throws DaoException {
		return buildList(findAllCachedEntities(ItemDef.KIND), ItemDef.class);
	}

	@Override
	public List<ItemDef> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		return buildList(getDatastore().get(keyList), ItemDef.class);
	}

}
