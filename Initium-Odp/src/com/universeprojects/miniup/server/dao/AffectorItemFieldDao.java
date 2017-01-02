package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.AffectorItemField;

public class AffectorItemFieldDao extends OdpDao<AffectorItemField> {

	public AffectorItemFieldDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public AffectorItemField get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new AffectorItemField(entity);
	}

	@Override
	public List<AffectorItemField> findAll() {
		List<AffectorItemField> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(AffectorItemField.KIND)) {
			all.add(new AffectorItemField(entity));
		}
		return all;
	}

}
