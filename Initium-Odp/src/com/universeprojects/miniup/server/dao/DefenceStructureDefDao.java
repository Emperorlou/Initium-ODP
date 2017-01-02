package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.DefenceStructureDef;

public class DefenceStructureDefDao extends OdpDao<DefenceStructureDef> {

	public DefenceStructureDefDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public DefenceStructureDef get(Key key) {
		DefenceStructureDef o = null;
		try {
			CachedEntity oEntity = getDatastore().get(key);
			if (oEntity != null) {
				o = new DefenceStructureDef(oEntity);
			}
		} catch (EntityNotFoundException e) {

		}
		return o;
	}
}
