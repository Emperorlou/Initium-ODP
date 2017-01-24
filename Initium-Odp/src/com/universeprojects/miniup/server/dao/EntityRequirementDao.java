package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.EntityRequirement;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class EntityRequirementDao extends OdpDao<EntityRequirement> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public EntityRequirementDao(CachedDatastoreService datastore) {
		super(datastore, EntityRequirement.KIND, EntityRequirement.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
