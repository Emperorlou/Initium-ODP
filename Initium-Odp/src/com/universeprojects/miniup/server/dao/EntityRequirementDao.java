package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
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
		EntityRequirement o = null;
		try {
			CachedEntity oEntity = getDatastore().get(key);
			if (oEntity != null) {
				o = new EntityRequirement(oEntity);
			}
		} catch (EntityNotFoundException e) {

		}
		return o;
	}
}
