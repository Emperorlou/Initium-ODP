package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.NPCArmy;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class NPCArmyDao extends OdpDao<NPCArmy> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public NPCArmyDao(CachedDatastoreService datastore) {
		super(datastore, NPCArmy.KIND, NPCArmy.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
