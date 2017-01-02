package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Discovery;
import com.universeprojects.miniup.server.exceptions.DaoException;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class DiscoveryDao extends OdpDao<Discovery> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public DiscoveryDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public Discovery get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new Discovery(entity);
	}

	@Override
	public List<Discovery> findAll() throws DaoException {
		return buildList(findAllCachedEntities(Discovery.KIND), Discovery.class);
	}

	@Override
	public List<Discovery> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		return buildList(getDatastore().get(keyList), Discovery.class);
	}

}
