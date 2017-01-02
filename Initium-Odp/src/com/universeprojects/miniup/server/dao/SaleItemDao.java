package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.SaleItem;

public class SaleItemDao extends OdpDao<SaleItem> {

public SaleItemDao(CachedDatastoreService datastore) {
super(datastore);
}

@Override
public SaleItem get(Key key) {
CachedEntity entity = getCachedEntity(key);
return entity == null ? null : new SaleItem(entity);
}@Override
public List<SaleItem> findAll() {
List<SaleItem> all = new ArrayList<>();
for (CachedEntity entity : findAllCachedEntities(SaleItem.KIND)) {
all.add(new SaleItem(entity));
}
return all;
}

}
