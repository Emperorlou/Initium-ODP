package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.EntityNotFoundException;
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
		AffectorItemField o = null;
		try {
			CachedEntity oEntity = getDatastore().get(key);
			if (oEntity != null) {
				o = new AffectorItemField(oEntity);
			}
		} catch (EntityNotFoundException e) {

		}
		return o;
	}
}
