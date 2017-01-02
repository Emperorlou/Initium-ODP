package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Quotes;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class QuotesDao extends OdpDao<Quotes> {

	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public QuotesDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public Quotes get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new Quotes(entity);
	}

	@Override
	public List<Quotes> findAll() {
		List<Quotes> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(Quotes.KIND)) {
			if (entity == null) {
				getLogger().warning("Null entity received from query");
				continue;
			}

			all.add(new Quotes(entity));
		}
		return all;
	}

}
