package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.DefenceStructureDef;
import com.universeprojects.miniup.server.exceptions.DaoException;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class DefenceStructureDefDao extends OdpDao<DefenceStructureDef> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public DefenceStructureDefDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public DefenceStructureDef get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new DefenceStructureDef(entity);
	}

	@Override
	public List<DefenceStructureDef> findAll() throws DaoException {
		return buildList(findAllCachedEntities(DefenceStructureDef.KIND), DefenceStructureDef.class);
	}

	@Override
	public List<DefenceStructureDef> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		return buildList(getDatastore().get(keyList), DefenceStructureDef.class);
	}

}
