package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.ConstructionToolRequirement;

import javassist.bytecode.stackmap.TypeData.ClassName;

public class ConstructionToolRequirementDao extends OdpDao<ConstructionToolRequirement> {

private static final Logger log = Logger.getLogger(ClassName.class.getName());

public ConstructionToolRequirementDao(CachedDatastoreService datastore) {
super(datastore);
}

@Override
protected Logger getLogger() {
return log;
}

@Override
public ConstructionToolRequirement get(Key key) {
CachedEntity entity = getCachedEntity(key);
return entity == null ? null : new ConstructionToolRequirement(entity);
}@Override
public List<ConstructionToolRequirement> findAll() {
List<ConstructionToolRequirement> all = new ArrayList<>();
for (CachedEntity entity : findAllCachedEntities(ConstructionToolRequirement.KIND)) {
if (entity == null) {
getLogger().warning("Null entity received from query");
continue;
}

all.add(new ConstructionToolRequirement(entity));
}
return all;
}

}
