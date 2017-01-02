package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.TransmuteRecipe;

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
	public List<TransmuteRecipe> findAll() {
		List<TransmuteRecipe> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(TransmuteRecipe.KIND)) {
			if (entity == null) {
				getLogger().warning("Null entity received from query");
				continue;
			}

			all.add(new TransmuteRecipe(entity));
		}
		return all;
	}

}
