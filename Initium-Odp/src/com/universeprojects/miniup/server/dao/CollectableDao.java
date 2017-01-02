package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Collectable;
import com.universeprojects.miniup.server.exceptions.DaoException;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class CollectableDao extends OdpDao<Collectable> {
private static final Logger log = Logger.getLogger(ClassName.class.getName());

public CollectableDao(CachedDatastoreService datastore) {
super(datastore);
}

@Override
protected Logger getLogger() {
return log;
}

@Override
public Collectable get(Key key) {
CachedEntity entity = getCachedEntity(key);
return entity == null ? null : new Collectable(entity);
}

@Override
public List<Collectable> findAll() throws DaoException {
return buildList(findAllCachedEntities(Collectable.KIND), Collectable.class);
}

@Override
public List<Collectable> get(List<Key> keyList) throws DaoException {
if (keyList == null || keyList.isEmpty()) {
return Collections.emptyList();
}

return buildList(getDatastore().get(keyList), Collectable.class);
}

}
