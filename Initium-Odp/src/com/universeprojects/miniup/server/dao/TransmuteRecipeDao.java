package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.TransmuteRecipe;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class TransmuteRecipeDao extends OdpDao<TransmuteRecipe> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public TransmuteRecipeDao(CachedDatastoreService datastore) {
		super(datastore, TransmuteRecipe.KIND, TransmuteRecipe.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
