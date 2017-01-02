package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.ChangeLog;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class ChangeLogDao extends OdpDao<ChangeLog> {

	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public ChangeLogDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public ChangeLog get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new ChangeLog(entity);
	}

	@Override
	public List<ChangeLog> findAll() {
		List<ChangeLog> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(ChangeLog.KIND)) {
			if (entity == null) {
				getLogger().warning("Null entity received from query");
				continue;
			}

			all.add(new ChangeLog(entity));
		}
		return all;
	}

}
