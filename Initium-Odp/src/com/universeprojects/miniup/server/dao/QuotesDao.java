package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Quotes;
import com.universeprojects.miniup.server.exceptions.DaoException;

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
	public List<Quotes> findAll() throws DaoException {
		return buildList(findAllCachedEntities(Quotes.KIND), Quotes.class);
	}

	@Override
	public List<Quotes> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		return buildList(getDatastore().get(keyList), Quotes.class);
	}

}
