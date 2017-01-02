package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.NPCArmy;

public class NPCArmyDao extends OdpDao<NPCArmy> {

	public NPCArmyDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public NPCArmy get(Key key) {
		NPCArmy o = null;
		try {
			CachedEntity oEntity = getDatastore().get(key);
			if (oEntity != null) {
				o = new NPCArmy(oEntity);
			}
		} catch (EntityNotFoundException e) {

		}
		return o;
	}
}
