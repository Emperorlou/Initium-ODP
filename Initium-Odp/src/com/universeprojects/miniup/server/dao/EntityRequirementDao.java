package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.EntityRequirement;
import com.universeprojects.miniup.server.exceptions.DaoException;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class EntityRequirementDao extends OdpDao<EntityRequirement> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public EntityRequirementDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public EntityRequirement get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new EntityRequirement(entity);
	}

	@Override
	public List<EntityRequirement> findAll() throws DaoException {
		return buildList(findAllCachedEntities(EntityRequirement.KIND), EntityRequirement.class);
	}

	@Override
	public List<EntityRequirement> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		return buildList(getDatastore().get(keyList), EntityRequirement.class);
	}

}
