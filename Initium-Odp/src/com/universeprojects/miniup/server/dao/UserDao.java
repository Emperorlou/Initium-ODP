package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.User;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class UserDao extends OdpDao<User> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public UserDao(CachedDatastoreService datastore) {
		super(datastore, User.KIND, User.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
