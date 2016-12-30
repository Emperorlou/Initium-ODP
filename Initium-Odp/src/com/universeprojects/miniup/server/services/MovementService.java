package com.universeprojects.miniup.server.services;

import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.QueryHelper;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class MovementService extends Service {

	private QueryHelper queryHelper;

	public MovementService(ODPDBAccess db) {
		super(db);
		this.queryHelper = new QueryHelper(super.db.getDB());
	}
	
	public void checkForLocks(CachedEntity character, CachedEntity pathToTake, Key destinationLocationKey) throws UserErrorMessage
	{
		Object lockCode = null;
		if (GameUtils.equals(destinationLocationKey, pathToTake.getProperty("location1Key")))
			lockCode = pathToTake.getProperty("location2LockCode");
		else if (GameUtils.equals(destinationLocationKey, pathToTake.getProperty("location2Key")))
			lockCode = pathToTake.getProperty("location1LockCode");
		else
			throw new RuntimeException("Player is not located at either end of the specified path.");
		
		if (lockCode != null) 
		{
			if (checkHasKey(character, (long)lockCode) == false)
				throw new UserErrorMessage("This location is locked. You must have the correct key in your inventory before you can access it.");
			else {
				FilterPredicate f1 = new FilterPredicate("containerKey", FilterOperator.EQUAL, character.getKey());
				FilterPredicate f2 = new FilterPredicate("keyCode", FilterOperator.EQUAL, (long)lockCode);
				CachedDatastoreService ds = db.getDB();
				
				List<CachedEntity> matchingKeys = ds.fetchAsList("Item", CompositeFilterOperator.and(f1, f2), 1000);
				
				// first matching key loses 1 durability
				CachedEntity key = matchingKeys.get(0);
				
				if (GameUtils.equals(key.getProperty("durability"), null) == false) {
					long durability = (long) key.getProperty("durability");
					
					if (durability > 1) {
						key.setProperty("durability", durability - 1);
						ds.put(key);
					}
					else
						ds.delete(key);
				}
			}
		}
	}
	
	private boolean checkHasKey(CachedEntity character, long lockCode) {
		int matchingKeys = db.getFilteredList_Count("Item", "containerKey", FilterOperator.EQUAL, character.getKey(), "keyCode", FilterOperator.EQUAL, (long)lockCode);
		
		return (matchingKeys > 0);
	}

	/**
	 * Returns true if the character has a Discovery for the path.
	 * 
	 * @param characterKey
	 * @param pathKey
	 * @return
	 */
	public boolean isPathDiscovered(Key characterKey, Key pathKey) {
		return queryHelper.getFilteredList_Count("Discovery", "characterKey", FilterOperator.EQUAL, characterKey, "entityKey", FilterOperator.EQUAL, pathKey) > 0;
	}
}
