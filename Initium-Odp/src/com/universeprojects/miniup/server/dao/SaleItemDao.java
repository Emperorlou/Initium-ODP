package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.SaleItem;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class SaleItemDao extends OdpDao<SaleItem> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public SaleItemDao(CachedDatastoreService datastore) {
		super(datastore, SaleItem.KIND, SaleItem.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
