package com.universeprojects.miniup.server.services;

import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;

public abstract class ODPKnowledgeService extends Service
{
	final protected Key characterKey;

	/**
	 * This will query for all knowledge entities associated with the character. 
	 * 
	 * This method will return null only if the number of knowledge entities is
	 * 100 or more. In this case, other techniques will need to be used to fetch
	 * the knowledge tree (like only fetching in parts). This limit is imposed 
	 * for UX purposes.
	 * 
	 * @return
	 */
	public ODPKnowledgeService(ODPDBAccess db, Key characterKey)
	{
		super(db);
		this.characterKey = characterKey;
	}

	
	/**
	 * This will query for all knowledge entities associated with the character. 
	 * 
	 * This method will return null only if the number of knowledge entities is
	 * 100 or more. In this case, other techniques will need to be used to fetch
	 * the knowledge tree (like only fetching in parts). This limit is imposed 
	 * for UX purposes.
	 * 
	 * @return
	 */
	public List<CachedEntity> getAllKnowledgeTree()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
