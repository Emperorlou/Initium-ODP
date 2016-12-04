package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;

public class InventionService extends Service
{
	final private CachedEntity character;
	
	private List<CachedEntity> availableItems = null;
	
	public InventionService(ODPDBAccess db, CachedEntity character)
	{
		super(db);
		this.character = character;
	}

	
	/**
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
		if (availableItems!=null)
			return availableItems;
		
		availableItems = db.getFilteredList("Item", "containerKey", character.getKey());
		
		availableItems.addAll(db.getFilteredList("Item", "containerKey", character.getProperty("locationKey")));
		
		return availableItems;
	}
	
	/**
	 * This will look at all available items to see if we can find any that match the given entityRequirement.
	 * We return all possible matches.
	 * 
	 * @param entityRequirement
	 * @param excludedItems
	 * @return
	 */
	public List<CachedEntity> getItemCandidatesFor(CachedEntity entityRequirement, Collection<CachedEntity> excludedItems)
	{
		getAvailableItems();
		
		Set<Key> excludedKeys = new HashSet<Key>();
		for(CachedEntity e:excludedItems)
			excludedKeys.add(e.getKey());
		
		List<CachedEntity> results = new ArrayList<CachedEntity>();
		for(CachedEntity candidate:availableItems)
		{
			if (excludedKeys.isEmpty()==false && excludedKeys.contains(candidate.getKey()))
				continue;
			
			if (db.validateEntityRequirement(entityRequirement, candidate))
				results.add(candidate);
		}
		
		return results;
	}
	
	
	
	

}
