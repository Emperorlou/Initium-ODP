package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.Tag;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class TagDao extends OdpDao<Tag> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public TagDao(CachedDatastoreService datastore) {
		super(datastore, Tag.KIND, Tag.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
