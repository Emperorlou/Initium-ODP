package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.ConstructionToolRequirement;
import com.universeprojects.miniup.server.exceptions.DaoException;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class ConstructionToolRequirementDao extends OdpDao<ConstructionToolRequirement> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public ConstructionToolRequirementDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public ConstructionToolRequirement get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new ConstructionToolRequirement(entity);
	}

	@Override
	public List<ConstructionToolRequirement> findAll() throws DaoException {
		return buildList(findAllCachedEntities(ConstructionToolRequirement.KIND), ConstructionToolRequirement.class);
	}

	@Override
	public List<ConstructionToolRequirement> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		return buildList(getDatastore().get(keyList), ConstructionToolRequirement.class);
	}

}
