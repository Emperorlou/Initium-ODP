package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
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
		Project o = null;
		try {
			CachedEntity oEntity = getDatastore().get(key);
			if (oEntity != null) {
				o = new Project(oEntity);
			}
		} catch (EntityNotFoundException e) {

		}
		return o;
	}
}
