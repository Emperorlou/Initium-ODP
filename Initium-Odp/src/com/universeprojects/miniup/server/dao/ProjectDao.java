package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Project;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class ProjectDao extends OdpDao<Project> {

	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public ProjectDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public Project get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new Project(entity);
	}

	@Override
	public List<Project> findAll() {
		List<Project> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(Project.KIND)) {
			if (entity == null) {
				getLogger().warning("Null entity received from query");
				continue;
			}

			all.add(new Project(entity));
		}
		return all;
	}

}
