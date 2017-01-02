package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Tag;
import com.universeprojects.miniup.server.exceptions.DaoException;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class TagDao extends OdpDao<Tag> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public TagDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public Tag get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new Tag(entity);
	}

	@Override
	public List<Tag> findAll() throws DaoException {
		return buildList(findAllCachedEntities(Tag.KIND), Tag.class);
	}

	@Override
	public List<Tag> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		return buildList(getDatastore().get(keyList), Tag.class);
	}

}
