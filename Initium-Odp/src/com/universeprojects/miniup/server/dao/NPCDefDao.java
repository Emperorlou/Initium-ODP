package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.NPCDef;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class NPCDefDao extends OdpDao<NPCDef> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public NPCDefDao(CachedDatastoreService datastore) {
		super(datastore, NPCDef.KIND, NPCDef.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
