package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.AssetAttribution;
import com.universeprojects.miniup.server.exceptions.DaoException;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class AssetAttributionDao extends OdpDao<AssetAttribution> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public AssetAttributionDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public AssetAttribution get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new AssetAttribution(entity);
	}

	@Override
	public List<AssetAttribution> findAll() throws DaoException {
		return buildList(findAllCachedEntities(AssetAttribution.KIND), AssetAttribution.class);
	}

	@Override
	public List<AssetAttribution> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		return buildList(getDatastore().get(keyList), AssetAttribution.class);
	}

}
