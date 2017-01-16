package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.Friend;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class FriendDao extends OdpDao<Friend> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public FriendDao(CachedDatastoreService datastore) {
		super(datastore, Friend.KIND, Friend.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
