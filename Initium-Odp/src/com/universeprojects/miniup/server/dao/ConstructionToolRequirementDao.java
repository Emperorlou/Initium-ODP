package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.ConstructionToolRequirement;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class ConstructionToolRequirementDao extends OdpDao<ConstructionToolRequirement> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public ConstructionToolRequirementDao(CachedDatastoreService datastore) {
		super(datastore, ConstructionToolRequirement.KIND, ConstructionToolRequirement.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
