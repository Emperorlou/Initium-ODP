package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;

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
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new ConstructionToolRequirement(entity);
	}

	@Override
	public List<ConstructionToolRequirement> findAll() {
		List<ConstructionToolRequirement> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(ConstructionToolRequirement.KIND)) {
			all.add(new ConstructionToolRequirement(entity));
		}
		return all;
	}

}
