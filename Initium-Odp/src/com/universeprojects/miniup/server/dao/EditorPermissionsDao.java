package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.EditorPermissions;
import com.universeprojects.miniup.server.exceptions.DaoException;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class EditorPermissionsDao extends OdpDao<EditorPermissions> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public EditorPermissionsDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public EditorPermissions get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new EditorPermissions(entity);
	}

	@Override
	public List<EditorPermissions> findAll() throws DaoException {
		return buildList(findAllCachedEntities(EditorPermissions.KIND), EditorPermissions.class);
	}

	@Override
	public List<EditorPermissions> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		return buildList(getDatastore().get(keyList), EditorPermissions.class);
	}

}
