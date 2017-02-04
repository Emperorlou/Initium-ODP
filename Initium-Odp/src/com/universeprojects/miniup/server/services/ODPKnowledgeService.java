package com.universeprojects.miniup.server.services;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.miniup.server.ODPDBAccess;

public abstract class ODPKnowledgeService extends Service
{
	final protected Key characterKey;

	public ODPKnowledgeService(ODPDBAccess db, Key characterKey)
	{
		super(db);
		this.characterKey = characterKey;
	}

}
