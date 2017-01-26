package com.universeprojects.miniup.server.services;

import java.util.List;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;

public class ItemConstructionService extends Service
{
	final private CachedEntity character;
	
	public ItemConstructionService(ODPDBAccess db, CachedEntity character)
	{
		super(db);
		this.character = character;
	}

	
	List<CachedEntity> allConstructionSkills = null;
	/**
	 * Gets all item construction skills available to the character. 
	 * 
	 * This call is cached and will only fetch once per instance of ItemConstructionService.
	 * @return
	 */
	public List<CachedEntity> getAllConstructionSkills()
	{
		if (allConstructionSkills!=null)
			return allConstructionSkills;
		
		allConstructionSkills = query.getFilteredList("ConstructItemSkill", "characterKey", character.getKey());
		return allConstructionSkills;
	}
	
}
