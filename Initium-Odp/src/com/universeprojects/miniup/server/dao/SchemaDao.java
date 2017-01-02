package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Schema;

public class SchemaDao extends OdpDao<Schema> {

public SchemaDao(CachedDatastoreService datastore) {
super(datastore);
}

@Override
public Schema get(Key key) {
CachedEntity entity = getCachedEntity(key);
return entity == null ? null : new Schema(entity);
}@Override
public List<Schema> findAll() {
List<Schema> all = new ArrayList<>();
for (CachedEntity entity : findAllCachedEntities(Schema.KIND)) {
all.add(new Schema(entity));
}
return all;
}

}
