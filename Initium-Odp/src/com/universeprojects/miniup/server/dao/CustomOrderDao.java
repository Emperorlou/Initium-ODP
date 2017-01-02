package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.CustomOrder;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class CustomOrderDao extends OdpDao<CustomOrder> {

private static final Logger log = Logger.getLogger(ClassName.class.getName());

public CustomOrderDao(CachedDatastoreService datastore) {
super(datastore);
}

@Override
protected Logger getLogger() {
return log;
}

@Override
public CustomOrder get(Key key) {
CachedEntity entity = getCachedEntity(key);
return entity == null ? null : new CustomOrder(entity);
}@Override
public List<CustomOrder> findAll() {
List<CustomOrder> all = new ArrayList<>();
for (CachedEntity entity : findAllCachedEntities(CustomOrder.KIND)) {
if (entity == null) {
getLogger().warning("Null entity received from query");
continue;
}

all.add(new CustomOrder(entity));
}
return all;
}

}
