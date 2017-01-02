package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.ConstructItemSkill;
import com.universeprojects.miniup.server.exceptions.DaoException;

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
	public List<ConstructItemSkill> findAll() throws DaoException {
		return buildList(findAllCachedEntities(ConstructItemSkill.KIND), ConstructItemSkill.class);
	}

	@Override
	public List<ConstructItemSkill> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		return buildList(getDatastore().get(keyList), ConstructItemSkill.class);
	}

}
