package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Schema;
import com.universeprojects.miniup.server.exceptions.DaoException;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class SchemaDao extends OdpDao<Schema> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public SchemaDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public Schema get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new Schema(entity);
	}

	@Override
	public List<Schema> findAll() throws DaoException {
		return buildList(findAllCachedEntities(Schema.KIND), Schema.class);
	}

	@Override
	public List<Schema> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		return buildList(getDatastore().get(keyList), Schema.class);
	}

}
