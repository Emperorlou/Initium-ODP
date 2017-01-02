package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Item;
import com.universeprojects.miniup.server.exceptions.DaoException;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class ItemDao extends OdpDao<Item> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public ItemDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public Item get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new Item(entity);
	}

	@Override
	public List<Item> findAll() throws DaoException {
		return buildList(findAllCachedEntities(Item.KIND), Item.class);
	}

	@Override
	public List<Item> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		return buildList(getDatastore().get(keyList), Item.class);
	}

}
