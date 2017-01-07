package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.EditorPermissions;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class EditorPermissionsDao extends OdpDao<EditorPermissions> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public EditorPermissionsDao(CachedDatastoreService datastore) {
		super(datastore, EditorPermissions.KIND, EditorPermissions.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
