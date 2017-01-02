package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.BuffDef;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class BuffDefDao extends OdpDao<BuffDef> {

	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public BuffDefDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public BuffDef get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new BuffDef(entity);
	}

	@Override
	public List<BuffDef> findAll() {
		List<BuffDef> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(BuffDef.KIND)) {
			if (entity == null) {
				getLogger().warning("Null entity received from query");
				continue;
			}

			all.add(new BuffDef(entity));
		}
		return all;
	}

}
