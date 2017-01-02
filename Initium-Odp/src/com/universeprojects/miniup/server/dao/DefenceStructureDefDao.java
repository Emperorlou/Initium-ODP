package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.DefenceStructureDef;

public class DefenceStructureDefDao extends OdpDao<DefenceStructureDef> {

public DefenceStructureDefDao(CachedDatastoreService datastore) {
super(datastore);
}

@Override
public DefenceStructureDef get(Key key) {
CachedEntity entity = getCachedEntity(key);
return entity == null ? null : new DefenceStructureDef(entity);
}@Override
public List<DefenceStructureDef> findAll() {
List<DefenceStructureDef> all = new ArrayList<>();
for (CachedEntity entity : findAllCachedEntities(DefenceStructureDef.KIND)) {
all.add(new DefenceStructureDef(entity));
}
return all;
}

}
