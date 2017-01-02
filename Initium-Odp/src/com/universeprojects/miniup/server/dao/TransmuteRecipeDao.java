package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.TransmuteRecipe;

public class TransmuteRecipeDao extends OdpDao<TransmuteRecipe> {

	public TransmuteRecipeDao(CachedDatastoreService datastore) {
		super(datastore);
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
			all.add(new TransmuteRecipe(entity));
		}
		return all;
	}

}
