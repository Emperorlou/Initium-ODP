package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.ItemDef;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class ItemDefDao extends OdpDao<ItemDef> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public ItemDefDao(CachedDatastoreService datastore) {
		super(datastore, ItemDef.KIND, ItemDef.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
