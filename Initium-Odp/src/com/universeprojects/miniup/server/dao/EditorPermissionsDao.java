package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.EditorPermissions;

public class EditorPermissionsDao extends OdpDao<EditorPermissions> {

	public EditorPermissionsDao(CachedDatastoreService datastore) {
		super(datastore);
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
			all.add(new EditorPermissions(entity));
		}
		return all;
	}

}
