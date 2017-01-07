package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.Buff;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class BuffDao extends OdpDao<Buff> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public BuffDao(CachedDatastoreService datastore) {
		super(datastore, Buff.KIND, Buff.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
