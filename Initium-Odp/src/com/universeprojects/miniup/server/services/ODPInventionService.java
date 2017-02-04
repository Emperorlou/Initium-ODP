package com.universeprojects.miniup.server.services;

import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;

public abstract class ODPInventionService extends Service
{
	final protected ODPKnowledgeService knowledgeService;
	
	protected ODPInventionService(ODPDBAccess db, ODPKnowledgeService knowledgeService)
	{
		super(db);
		this.knowledgeService = knowledgeService;
	}
	
	public ODPKnowledgeService getKnowledgeService()
	{
		return knowledgeService;
	}

	/**This is a placeholder. The implementation of this method is in the core repo.
	 * 
	 * This method will look at the character's location, and his inventory to 
	 * find all the items that are available to him. It will sort the items by
	 * inventory first, then location so that other processes can give priority
	 * to things that are on-hand rather than in the environment.
	 * 
	 * The database will only be hit the first time, subsequently a cached result
	 * will be returned.
	 * 
	 * @return
	 */
	public List<CachedEntity> getAvailableItems()
	{
		return null;
	}

	public List<CachedEntity> getAllItemConstructionIdeas()
	{
		return null;
	}
	
	public Map<Key, CachedEntity> getAllItemDefsForItemConstructionIdeas()
	{
		return null;
	}


}
