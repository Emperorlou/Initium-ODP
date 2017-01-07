package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.Item;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class ItemDao extends OdpDao<Item> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public ItemDao(CachedDatastoreService datastore) {
		super(datastore, Item.KIND, Item.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
