package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.ConstructItemSkill;

public class ConstructItemSkillDao extends OdpDao<ConstructItemSkill> {

	public ConstructItemSkillDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public ConstructItemSkill get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new ConstructItemSkill(entity);
	}

	@Override
	public List<ConstructItemSkill> findAll() {
		List<ConstructItemSkill> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(ConstructItemSkill.KIND)) {
			all.add(new ConstructItemSkill(entity));
		}
		return all;
	}

}
