package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.EditorPermissions;

public class EditorPermissionsDao extends OdpDao<EditorPermissions> {

	private static final Logger log = Logger.getLogger("EditorPermissionsDao");

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
	public List<EditorPermissions> findAll() {
		List<EditorPermissions> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(EditorPermissions.KIND)) {
			if (entity == null) {
				getLogger().warning("Null entity received from query");
				continue;
			}

			all.add(new EditorPermissions(entity));
		}
		return all;
	}

}
