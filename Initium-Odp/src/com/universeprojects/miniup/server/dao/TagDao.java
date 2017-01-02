package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Tag;

public class TagDao extends OdpDao<Tag> {

	private static final Logger log = Logger.getLogger("TagDao");

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
	public List<Tag> findAll() {
		List<Tag> all = new ArrayList<>();
		for (CachedEntity entity : findAllCachedEntities(Tag.KIND)) {
			if (entity == null) {
				getLogger().warning("Null entity received from query");
				continue;
			}

			all.add(new Tag(entity));
		}
		return all;
	}

}
