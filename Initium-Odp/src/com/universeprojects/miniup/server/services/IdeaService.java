package com.universeprojects.miniup.server.services;

import java.util.List;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;

public class IdeaService extends Service
{
	final private CachedEntity character;
	
	public IdeaService(ODPDBAccess db, CachedEntity character)
	{
		super(db);
		this.character = character;
	}

	
	List<CachedEntity> allIdeas = null;
	/**
	 * Gets all ideas available to the character. 
	 * 
	 * This call is cached and will only fetch once per instance of IdeaService.
	 * @return
	 */
	public List<CachedEntity> getAllIdeas()
	{
		if (allIdeas!=null)
			return allIdeas;
		
		allIdeas = query.getFilteredList("ConstructItemIdea", "characterKey", character.getKey());
		return allIdeas;
	}
	
	
	
	
	
}
