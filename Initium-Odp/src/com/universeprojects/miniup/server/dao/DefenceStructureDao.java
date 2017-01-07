package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.DefenceStructure;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class DefenceStructureDao extends OdpDao<DefenceStructure> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public DefenceStructureDao(CachedDatastoreService datastore) {
		super(datastore, DefenceStructure.KIND, DefenceStructure.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
