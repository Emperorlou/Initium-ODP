package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.EntityRequirement;

public class EntityRequirementDao extends OdpDao<EntityRequirement> {

	public EntityRequirementDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public EntityRequirement get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new EntityRequirement(entity);
	}
}
