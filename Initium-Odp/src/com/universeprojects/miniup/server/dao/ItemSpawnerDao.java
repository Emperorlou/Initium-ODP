package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.ItemSpawner;
import com.universeprojects.miniup.server.exceptions.DaoException;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class ItemSpawnerDao extends OdpDao<ItemSpawner> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public ItemSpawnerDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public ItemSpawner get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new ItemSpawner(entity);
	}

	@Override
	public List<ItemSpawner> findAll() throws DaoException {
		return buildList(findAllCachedEntities(ItemSpawner.KIND), ItemSpawner.class);
	}

	@Override
	public List<ItemSpawner> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		return buildList(getDatastore().get(keyList), ItemSpawner.class);
	}

}
