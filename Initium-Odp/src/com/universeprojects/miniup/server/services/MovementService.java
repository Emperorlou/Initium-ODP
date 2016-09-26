package com.universeprojects.miniup.server.services;

import com.google.appengine.api.datastore.Query.FilterOperator;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;

public class MovementService extends Service {
	
	public MovementService(ODPDBAccess db) {
		super(db);
	}
	
	public boolean checkKeyCode(CachedEntity path) {
		CachedEntity character = db.getCurrentCharacter();
		Object location = character.getProperty("locationKey");
		Object lockCode = null;
		
		if (GameUtils.equals(location, path.getProperty("location1Key")))
			lockCode = path.getProperty("location2LockCode");
		else if (GameUtils.equals(location, path.getProperty("location2Key")))
			lockCode = path.getProperty("location1LockCode");
		else
			throw new RuntimeException("Player is not located at either end of the specified path.");
		
		int matchingKeys = db.getFilteredList_Count("item", "containerKey", FilterOperator.EQUAL, character.getKey(), "keyCode", FilterOperator.EQUAL, (long)lockCode);
		
		return (matchingKeys > 0);
	}
}