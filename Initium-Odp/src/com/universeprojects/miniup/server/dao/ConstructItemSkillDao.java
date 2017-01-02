package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.ConstructItemSkill;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class ConstructItemSkillDao extends OdpDao<ConstructItemSkill> {

	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public ConstructItemSkillDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
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
			if (entity == null) {
				getLogger().warning("Null entity received from query");
				continue;
			}

			all.add(new ConstructItemSkill(entity));
		}
		return all;
	}

}
