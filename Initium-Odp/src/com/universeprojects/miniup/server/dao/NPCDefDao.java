package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
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
		NPCDef o = null;
		try {
			CachedEntity oEntity = getDatastore().get(key);
			if (oEntity != null) {
				o = new NPCDef(oEntity);
			}
		} catch (EntityNotFoundException e) {

		}
		return o;
	}
}
