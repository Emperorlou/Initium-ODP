package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.AffectorItemField;

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
	public List<AffectorItemField> findAll() {
		List<AffectorItemField> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(AffectorItemField.KIND)) {
			if (entity == null) {
				getLogger().warning("Null entity received from query");
				continue;
			}

			all.add(new AffectorItemField(entity));
		}
		return all;
	}

}
