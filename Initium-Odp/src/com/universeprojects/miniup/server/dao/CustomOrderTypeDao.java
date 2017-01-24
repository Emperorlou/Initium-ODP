package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.CustomOrderType;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class CustomOrderTypeDao extends OdpDao<CustomOrderType> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public CustomOrderTypeDao(CachedDatastoreService datastore) {
		super(datastore, CustomOrderType.KIND, CustomOrderType.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
