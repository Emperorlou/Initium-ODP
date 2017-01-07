package com.universeprojects.miniup.server.dao;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.ChangeLog;
import com.universeprojects.miniup.server.exceptions.DaoException;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class ChangeLogDao extends OdpDao<ChangeLog> {
private static final Logger log = Logger.getLogger(ClassName.class.getName());

public ChangeLogDao(CachedDatastoreService datastore) {
super(datastore);
}

@Override
protected Logger getLogger() {
return log;
}

@Override
public ChangeLog get(Key key) {
CachedEntity entity = getCachedEntity(key);
return entity == null ? null : new ChangeLog(entity);
}

@Override
public List<ChangeLog> findAll() throws DaoException {
return buildList(findAllCachedEntities(ChangeLog.KIND), ChangeLog.class);
}

@Override
public List<ChangeLog> get(List<Key> keyList) throws DaoException {
if (keyList == null || keyList.isEmpty()) {
return Collections.emptyList();
}

return buildList(getDatastore().get(keyList), ChangeLog.class);
}

}