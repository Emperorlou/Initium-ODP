package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.Project;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class ProjectDao extends OdpDao<Project> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public ProjectDao(CachedDatastoreService datastore) {
		super(datastore, Project.KIND, Project.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
