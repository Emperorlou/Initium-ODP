package com.universeprojects.miniup.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.universeprojects.cacheddatastore.CachedEntity;

public class InitiumObject
{
	final ODPDBAccess db;
	final CachedEntity entity;
	Map<String, InitiumAspect> aspects;
	
	@SuppressWarnings("unchecked")
	public InitiumObject(ODPDBAccess db, CachedEntity entity)
	{
		this.db = db;
		this.entity = entity;
		Set<String> aspectIds = (Set<String>)entity.getProperty("_aspects");
		if (aspectIds!=null)
		{
			this.aspects = new HashMap<String, InitiumAspect>();
			
			for(String aspectId:aspectIds)
				aspects.put(aspectId, (InitiumAspect)GameUtils.createObject("com.universeprojects.miniup.server.aspects.Aspect"+aspectId));
		}
		else
		{
			this.aspects = null;
		}
	}
	
	public CachedEntity getEntity()
	{
		return entity;
	}
	
	public ODPDBAccess getDB()
	{
		return db;
	}
	
	public void addAspect(String aspectId)
	{
		aspects.put(aspectId, (InitiumAspect)GameUtils.createObject("com.universeprojects.miniup.server.aspects.Aspect"+aspectId));
		@SuppressWarnings("unchecked")
		Set<String> aspectIds = (Set<String>)entity.getProperty("_aspects");
		aspectIds.add(aspectId);
		entity.setPropertyManually("_aspects", aspectIds);
	}

}
