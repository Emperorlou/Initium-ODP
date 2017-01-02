package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
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
		TransmuteRecipe o = null;
		try {
			CachedEntity oEntity = getDatastore().get(key);
			if (oEntity != null) {
				o = new TransmuteRecipe(oEntity);
			}
		} catch (EntityNotFoundException e) {

		}
		return o;
	}
}
