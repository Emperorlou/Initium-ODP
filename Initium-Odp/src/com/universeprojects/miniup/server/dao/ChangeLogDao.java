package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.ChangeLog;

public class ChangeLogDao extends OdpDao<ChangeLog> {

public ChangeLogDao(CachedDatastoreService datastore) {
super(datastore);
}

@Override
public ChangeLog get(Key key) {
CachedEntity entity = getCachedEntity(key);
return entity == null ? null : new ChangeLog(entity);
}@Override
public List<ChangeLog> findAll() {
List<ChangeLog> all = new ArrayList<>();
for (CachedEntity entity : findAllCachedEntities(ChangeLog.KIND)) {
all.add(new ChangeLog(entity));
}
return all;
}

}
