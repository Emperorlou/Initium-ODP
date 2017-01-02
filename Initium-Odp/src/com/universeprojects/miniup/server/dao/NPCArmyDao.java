package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.NPCArmy;

public class NPCArmyDao extends OdpDao<NPCArmy> {

	public NPCArmyDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public NPCArmy get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new NPCArmy(entity);
	}

	@Override
	public List<NPCArmy> findAll() {
		List<NPCArmy> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(NPCArmy.KIND)) {
			all.add(new NPCArmy(entity));
		}
		return all;
	}

}
