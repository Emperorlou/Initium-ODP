package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.ChangeLog;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class ChangeLogDao extends OdpDao<ChangeLog> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public ChangeLogDao(CachedDatastoreService datastore) {
		super(datastore, ChangeLog.KIND, ChangeLog.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
