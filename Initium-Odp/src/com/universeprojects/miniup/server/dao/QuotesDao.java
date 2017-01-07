package com.universeprojects.miniup.server.dao;

import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.domain.Quotes;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class QuotesDao extends OdpDao<Quotes> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public QuotesDao(CachedDatastoreService datastore) {
		super(datastore, Quotes.KIND, Quotes.class);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

}
