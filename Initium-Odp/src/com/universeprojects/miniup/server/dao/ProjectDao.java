package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Project;

public class ProjectDao extends OdpDao<Project> {

	public ProjectDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public Project get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new Project(entity);
	}
}
