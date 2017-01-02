package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.CollectableDef;

public class CollectableDefDao extends OdpDao<CollectableDef> {

public CollectableDefDao(CachedDatastoreService datastore) {
super(datastore);
}

@Override
public CollectableDef get(Key key) {
CachedEntity entity = getCachedEntity(key);
return entity == null ? null : new CollectableDef(entity);
}@Override
public List<CollectableDef> findAll() {
List<CollectableDef> all = new ArrayList<>();
for (CachedEntity entity : findAllCachedEntities(CollectableDef.KIND)) {
all.add(new CollectableDef(entity));
}
return all;
}

}
