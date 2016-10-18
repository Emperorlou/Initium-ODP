package com.universeprojects.miniup.server.services;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class MovementService extends Service {
	
	public MovementService(ODPDBAccess db) {
		super(db);
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
			if (checkHasKey(character, (long)lockCode)==false)
				throw new UserErrorMessage("This location is locked. You must have the correct key in your inventory before you can access it.");
		}
	}
	
	private boolean checkHasKey(CachedEntity character, long lockCode) {
		int matchingKeys = db.getFilteredList_Count("Item", "containerKey", FilterOperator.EQUAL, character.getKey(), "keyCode", FilterOperator.EQUAL, (long)lockCode);
		
		return (matchingKeys > 0);
	}
	
}