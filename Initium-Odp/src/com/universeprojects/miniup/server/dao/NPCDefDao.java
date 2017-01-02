package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.NPCDef;

public class NPCDefDao extends OdpDao<NPCDef> {

private static final Logger log = Logger.getLogger("NPCDefDao");

public NPCDefDao(CachedDatastoreService datastore) {
super(datastore);
}

@Override
protected Logger getLogger() {
return log;
}

@Override
public NPCDef get(Key key) {
CachedEntity entity = getCachedEntity(key);
return entity == null ? null : new NPCDef(entity);
}@Override
public List<NPCDef> findAll() {
List<NPCDef> all = new ArrayList<>();
for (CachedEntity entity : findAllCachedEntities(NPCDef.KIND)) {
if (entity == null) {
getLogger().warning("Null entity received from query");
continue;
}

all.add(new NPCDef(entity));
}
return all;
}

}
