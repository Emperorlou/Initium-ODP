package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Character;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class CharacterDao extends OdpDao<Character> {

	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public CharacterDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
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
			if (entity == null) {
				getLogger().warning("Null entity received from query");
				continue;
			}

			all.add(new Character(entity));
		}
		return all;
	}

}
