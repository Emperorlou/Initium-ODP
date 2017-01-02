package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.AffectorProcess;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class AffectorProcessDao extends OdpDao<AffectorProcess> {

	private static final Logger log = Logger.getLogger(ClassName.class.getName());

	public AffectorProcessDao(CachedDatastoreService datastore) {
		super(datastore);
	}

	@Override
	protected Logger getLogger() {
		return log;
	}

	@Override
	public AffectorProcess get(Key key) {
		CachedEntity entity = getCachedEntity(key);
		return entity == null ? null : new AffectorProcess(entity);
	}

	@Override
	public List<AffectorProcess> findAll() {
		List<AffectorProcess> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(AffectorProcess.KIND)) {
			if (entity == null) {
				getLogger().warning("Null entity received from query");
				continue;
			}

			all.add(new AffectorProcess(entity));
		}
		return all;
	}

}
