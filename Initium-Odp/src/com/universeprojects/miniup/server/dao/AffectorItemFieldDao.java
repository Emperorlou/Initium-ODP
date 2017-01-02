package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.AffectorItemField;
import com.universeprojects.miniup.server.exceptions.DaoException;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class AffectorItemFieldDao extends OdpDao<AffectorItemField> {
	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public AffectorItemFieldDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public AffectorItemField get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new AffectorItemField(entity);
	}

	@Override
	public List<AffectorItemField> findAll() throws DaoException {
		return buildList(findAllCachedEntities(AffectorItemField.KIND), AffectorItemField.class);
	}

	@Override
	public List<AffectorItemField> get(List<Key> keyList) throws DaoException {
		if (keyList == null || keyList.isEmpty()) {
			return Collections.emptyList();
		}

		return buildList(getDatastore().get(keyList), AffectorItemField.class);
	}

}
