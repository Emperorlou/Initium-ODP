package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.NPCArmy;
import com.universeprojects.miniup.server.exceptions.DaoException;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class NPCArmyDao extends OdpDao<NPCArmy> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public NPCArmyDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public NPCArmy get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new NPCArmy(entity);
	}

	@Override
	public List<NPCArmy> findAll() throws DaoException {
		return buildList(findAllCachedEntities(NPCArmy.KIND), NPCArmy.class);
	}

	@Override
	public List<NPCArmy> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		return buildList(getDatastore().get(keyList), NPCArmy.class);
	}

}
