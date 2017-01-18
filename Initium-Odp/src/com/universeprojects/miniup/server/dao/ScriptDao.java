package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.Script;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class ScriptDao extends OdpDao<Script> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public ScriptDao(CachedDatastoreService datastore) {
		super(datastore, Script.KIND, Script.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
