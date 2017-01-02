package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.AffectorProcess;

public class AffectorProcessDao extends OdpDao<AffectorProcess> {

	public AffectorProcessDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public AffectorProcess get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new AffectorProcess(entity);
	}
}
