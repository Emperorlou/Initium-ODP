package com.universeprojects.miniup.server;

import java.util.List;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;

public class ODPGameFunctions extends CachedDatastoreService
{
	final CachedDatastoreService ds;
	
	public ODPGameFunctions(CachedDatastoreService ds) 
	{
		super();
		this.ds = ds;
	}
	

	/**
	 * Gets an entity from the database by it's kind and ID. If no entity was found,
	 * this method will simply return null. 
	 * 
	 * @param kind
	 * @param id
	 * @return
	 */
	public CachedEntity getEntity(String kind, Long id)
	{
		Key key = createKey(kind, id);
		try 
		{
			return ds.get(key);
		} catch (EntityNotFoundException e) 
		{
			return null;
		}
	}
	
	/**
	 * Creates a datastore key out of a kind and ID.
	 * 
	 * @param kind
	 * @param id
	 * @return
	 */
	public Key createKey(String kind, Long id)
	{
		return KeyFactory.createKey(kind, id);
	}

	/**
	 * EFFICIENTLY fetches a bunch of entities from a list of keys.
	 * 
	 * If it is possible to use this, please use it instead of calling getEntity() back to back.
	 * 
	 * @param keyList
	 * @return
	 */
	public List<CachedEntity> getEntities(List<Key> keyList)
	{
		return ds.fetchEntitiesFromKeys(keyList);
	}
	
	
	/**
	 * EFFICIENTLY fetches a bunch of entities from a list of keys.
	 * 
	 * If it is possible to use this, please use it instead of calling getEntity() back to back.
	 * 
	 * @param keyList
	 * @return
	 */
	public List<CachedEntity> getEntities(Key...keyList)
	{
		return ds.fetchEntitiesFromKeys(keyList);
	}
	
}
