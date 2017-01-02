package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Path;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class PathDao extends OdpDao<Path> {

	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public PathDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public Path get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new Path(entity);
	}

	@Override
	public List<Path> findAll() {
		List<Path> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(Path.KIND)) {
			if (entity == null) {
				getLogger().warning("Null entity received from query");
				continue;
			}

			all.add(new Path(entity));
		}
		return all;
	}

}
