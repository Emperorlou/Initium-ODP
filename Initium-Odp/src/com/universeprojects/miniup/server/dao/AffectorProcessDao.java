package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;

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

	@Override
	public List<AffectorProcess> findAll() {
		List<AffectorProcess> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(AffectorProcess.KIND)) {
			all.add(new AffectorProcess(entity));
		}
		return all;
	}

}
