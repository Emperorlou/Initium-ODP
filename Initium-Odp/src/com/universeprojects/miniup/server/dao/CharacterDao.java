package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Character;
import com.universeprojects.miniup.server.exceptions.DaoException;

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
	public List<Character> findAll() throws DaoException {
		return buildList(findAllCachedEntities(Character.KIND), Character.class);
	}

	@Override
	public List<Character> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		return buildList(getDatastore().get(keyList), Character.class);
	}

}
