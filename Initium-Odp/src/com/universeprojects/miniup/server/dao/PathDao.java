package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Path;
import com.universeprojects.miniup.server.exceptions.DaoException;

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
	public List<Path> findAll() throws DaoException {
		return buildList(findAllCachedEntities(Path.KIND), Path.class);
	}

	@Override
	public List<Path> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		return buildList(getDatastore().get(keyList), Path.class);
	}

}
