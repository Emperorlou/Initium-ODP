package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.Character;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class CharacterDao extends OdpDao<Character> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public CharacterDao(CachedDatastoreService datastore) {
		super(datastore, Character.KIND, Character.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
