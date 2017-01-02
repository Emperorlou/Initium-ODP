package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.NPCDef;
import com.universeprojects.miniup.server.exceptions.DaoException;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class NPCDefDao extends OdpDao<NPCDef> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public NPCDefDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public NPCDef get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new NPCDef(entity);
	}

	@Override
	public List<NPCDef> findAll() throws DaoException {
		return buildList(findAllCachedEntities(NPCDef.KIND), NPCDef.class);
	}

	@Override
	public List<NPCDef> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		return buildList(getDatastore().get(keyList), NPCDef.class);
	}

}
