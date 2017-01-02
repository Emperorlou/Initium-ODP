package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Affector;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class AffectorDao extends OdpDao<Affector> {

	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public AffectorDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public Affector get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new Affector(entity);
	}

	@Override
	public List<Affector> findAll() {
		List<Affector> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(Affector.KIND)) {
			if (entity == null) {
				getLogger().warning("Null entity received from query");
				continue;
			}

			all.add(new Affector(entity));
		}
		return all;
	}

}
