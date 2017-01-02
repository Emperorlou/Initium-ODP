package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;

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

	@Override
	public List<Character> findAll() {
		List<Character> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(Character.KIND)) {
			all.add(new Character(entity));
		}
		return all;
	}

}
