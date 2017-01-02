package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.ChangeLog;

public class ChangeLogDao extends OdpDao<ChangeLog> {

	public ChangeLogDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public ChangeLog get(Key key) {
		ChangeLog o = null;
		try {
			CachedEntity oEntity = getDatastore().get(key);
			if (oEntity != null) {
				o = new ChangeLog(oEntity);
			}
		} catch (EntityNotFoundException e) {

		}
		return o;
	}
}
