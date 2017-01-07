package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.ConstructItemSkill;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class ConstructItemSkillDao extends OdpDao<ConstructItemSkill> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public ConstructItemSkillDao(CachedDatastoreService datastore) {
		super(datastore, ConstructItemSkill.KIND, ConstructItemSkill.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
