package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.ConstructItemSkill;

public class ConstructItemSkillDao extends OdpDao<ConstructItemSkill> {

	public ConstructItemSkillDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public ConstructItemSkill get(Key key) {
		ConstructItemSkill o = null;
		try {
			CachedEntity oEntity = getDatastore().get(key);
			if (oEntity != null) {
				o = new ConstructItemSkill(oEntity);
			}
		} catch (EntityNotFoundException e) {

		}
		return o;
	}
}
