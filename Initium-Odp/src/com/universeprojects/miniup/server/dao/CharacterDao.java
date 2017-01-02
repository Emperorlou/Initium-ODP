package com.universeprojects.miniup.server.dao;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Character;

public class CharacterDao extends OdpDao<Character> {

	public CharacterDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	public Character get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new Character(entity);
	}
}
