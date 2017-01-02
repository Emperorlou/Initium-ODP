package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
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
		EditorPermissions o = null;
		try {
			CachedEntity oEntity = getDatastore().get(key);
			if (oEntity != null) {
				o = new EditorPermissions(oEntity);
			}
		} catch (EntityNotFoundException e) {

		}
		return o;
	}
}
