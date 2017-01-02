package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.DefenceStructure;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class DefenceStructureDao extends OdpDao<DefenceStructure> {

	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public DefenceStructureDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
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
			if (entity == null) {
				getLogger().warning("Null entity received from query");
				continue;
			}

			all.add(new DefenceStructure(entity));
		}
		return all;
	}

}
