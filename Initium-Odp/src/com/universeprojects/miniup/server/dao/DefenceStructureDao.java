package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.DefenceStructure;

public class DefenceStructureDao extends OdpDao<DefenceStructure> {

	public DefenceStructureDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public DefenceStructure get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new DefenceStructure(entity);
	}

	@Override
	public List<DefenceStructure> findAll() {
		List<DefenceStructure> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(DefenceStructure.KIND)) {
			all.add(new DefenceStructure(entity));
		}
		return all;
	}

}
