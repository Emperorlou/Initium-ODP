package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.BuffDef;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class BuffDefDao extends OdpDao<BuffDef> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public BuffDefDao(CachedDatastoreService datastore) {
		super(datastore, BuffDef.KIND, BuffDef.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
