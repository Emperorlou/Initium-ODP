package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.NPCDef;

public class NPCDefDao extends OdpDao<NPCDef> {

	public NPCDefDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public NPCDef get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new NPCDef(entity);
	}
}
