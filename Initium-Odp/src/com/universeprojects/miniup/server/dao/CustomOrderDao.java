package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.CustomOrder;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class CustomOrderDao extends OdpDao<CustomOrder> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public CustomOrderDao(CachedDatastoreService datastore) {
		super(datastore, CustomOrder.KIND, CustomOrder.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
