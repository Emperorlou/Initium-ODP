package com.universeprojects.miniup.server.services;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.OperationBase;

public class GamePageUpdateService extends MainPageUpdateService {

	public GamePageUpdateService(ODPDBAccess db, CachedEntity user,
			CachedEntity character, CachedEntity location,
			OperationBase operation) {
		super(db, user, character, location, operation);
	}
}
