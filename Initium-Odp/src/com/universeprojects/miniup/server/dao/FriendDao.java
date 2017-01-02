package com.universeprojects.miniup.server.dao;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.domain.Friend;

public class FriendDao extends OdpDao<Friend> {

public FriendDao(CachedDatastoreService datastore) {
super(datastore);
}

@Override
public Friend get(Key key) {
CachedEntity entity = getCachedEntity(key);
return entity == null ? null : new Friend(entity);
}@Override
public List<Friend> findAll() {
List<Friend> all = new ArrayList<>();
for (CachedEntity entity : findAllCachedEntities(Friend.KIND)) {
all.add(new Friend(entity));
}
return all;
}

}
