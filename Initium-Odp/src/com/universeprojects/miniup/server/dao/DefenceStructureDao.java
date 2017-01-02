package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.DefenceStructure;

public class DefenceStructureDao extends OdpDao<DefenceStructure> {

	public DefenceStructureDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public DefenceStructure get(Key key) {
		DefenceStructure o = null;
		try {
			CachedEntity oEntity = getDatastore().get(key);
			if (oEntity != null) {
				o = new DefenceStructure(oEntity);
			}
		} catch (EntityNotFoundException e) {

		}
		return o;
	}
}
