package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.BuffDef;

public class BuffDefDao extends OdpDao<BuffDef> {

public BuffDefDao(CachedDatastoreService datastore) {
super(datastore);
}

@Override
public BuffDef get(Key key) {
CachedEntity entity = getCachedEntity(key);
return entity == null ? null : new BuffDef(entity);
}@Override
public List<BuffDef> findAll() {
List<BuffDef> all = new ArrayList<>();
for (CachedEntity entity : findAllCachedEntities(BuffDef.KIND)) {
all.add(new BuffDef(entity));
}
return all;
}

}
