package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Collectable;

public class CollectableDao extends OdpDao<Collectable> {

public CollectableDao(CachedDatastoreService datastore) {
super(datastore);
}

@Override
public Collectable get(Key key) {
CachedEntity entity = getCachedEntity(key);
return entity == null ? null : new Collectable(entity);
}@Override
public List<Collectable> findAll() {
List<Collectable> all = new ArrayList<>();
for (CachedEntity entity : findAllCachedEntities(Collectable.KIND)) {
all.add(new Collectable(entity));
}
return all;
}

}
