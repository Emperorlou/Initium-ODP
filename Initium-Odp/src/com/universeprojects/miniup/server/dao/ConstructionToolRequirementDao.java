package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.ConstructionToolRequirement;

public class ConstructionToolRequirementDao extends OdpDao<ConstructionToolRequirement> {

	public ConstructionToolRequirementDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public ConstructionToolRequirement get(Key key) {
		ConstructionToolRequirement o = null;
		try {
			CachedEntity oEntity = getDatastore().get(key);
			if (oEntity != null) {
				o = new ConstructionToolRequirement(oEntity);
			}
		} catch (EntityNotFoundException e) {

		}
		return o;
	}
}
