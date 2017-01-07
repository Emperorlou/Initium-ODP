package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.Group;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class GroupDao extends OdpDao<Group> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public GroupDao(CachedDatastoreService datastore) {
		super(datastore, Group.KIND, Group.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
