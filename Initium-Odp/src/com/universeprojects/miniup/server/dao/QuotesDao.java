package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Quotes;

public class QuotesDao extends OdpDao<Quotes> {

public QuotesDao(CachedDatastoreService datastore) {
super(datastore);
}

@Override
public Quotes get(Key key) {
CachedEntity entity = getCachedEntity(key);
return entity == null ? null : new Quotes(entity);
}@Override
public List<Quotes> findAll() {
List<Quotes> all = new ArrayList<>();
for (CachedEntity entity : findAllCachedEntities(Quotes.KIND)) {
all.add(new Quotes(entity));
}
return all;
}

}
