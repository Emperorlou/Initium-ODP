package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.TransmuteRecipe;
import com.universeprojects.miniup.server.exceptions.DaoException;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class TransmuteRecipeDao extends OdpDao<TransmuteRecipe> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public TransmuteRecipeDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public TransmuteRecipe get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new TransmuteRecipe(entity);
	}

	@Override
	public List<TransmuteRecipe> findAll() throws DaoException {
		return buildList(findAllCachedEntities(TransmuteRecipe.KIND), TransmuteRecipe.class);
	}

	@Override
	public List<TransmuteRecipe> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		return buildList(getDatastore().get(keyList), TransmuteRecipe.class);
	}

}
