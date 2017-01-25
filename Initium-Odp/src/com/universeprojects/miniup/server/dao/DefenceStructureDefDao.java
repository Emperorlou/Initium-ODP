package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.DefenceStructureDef;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class DefenceStructureDefDao extends OdpDao<DefenceStructureDef> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public DefenceStructureDefDao(CachedDatastoreService datastore) {
		super(datastore, DefenceStructureDef.KIND, DefenceStructureDef.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
