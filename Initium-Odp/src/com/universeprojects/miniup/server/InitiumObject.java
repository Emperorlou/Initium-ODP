package com.universeprojects.miniup.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.gefcommon.shared.elements.GameAspect;
import com.universeprojects.gefcommon.shared.elements.GameObject;
import com.universeprojects.miniup.server.services.GridMapService;

public class InitiumObject implements GameObject<Key>
{
	private GridMapService gms;
	final protected ODPDBAccess db;
	final protected CachedEntity entity;
	protected Map<String, InitiumAspect> aspects;
	
	@SuppressWarnings("unchecked")
	public InitiumObject(ODPDBAccess db, CachedEntity entity)
	{
		this.db = db;
		this.entity = entity;
		
		Object aspectsObj = entity.getProperty("_aspects");
		Set<String> aspectIds = null;
		if (aspectsObj instanceof Set)
			aspectIds = (Set<String>)aspectsObj;
		else if (aspectsObj instanceof List)
			aspectIds = new LinkedHashSet<String>((List<String>)aspectsObj);
		
		if (aspectIds!=null)
		{
			this.aspects = new HashMap<String, InitiumAspect>();
			
			for(String aspectId:aspectIds)
				aspects.put(aspectId, (InitiumAspect)GameUtils.createObject("com.universeprojects.miniup.server.aspects.Aspect"+aspectId, this));
		}
		else
		{
			this.aspects = null;
		}
	}
	
	public static List<InitiumObject> wrap(ODPDBAccess db, List<CachedEntity> entities)
	{
		List<InitiumObject> result = new ArrayList<InitiumObject>();
		
		for(CachedEntity entity:entities)
			result.add(new InitiumObject(db, entity));
		
		return result;
	}
	
	public CachedEntity getEntity()
	{
		return entity;
	}
	
	public ODPDBAccess getDB()
	{
		return db;
	}
	
	private String aspectClassToString(Class<? extends InitiumAspect> aspectClass)
	{
		return aspectClass.getSimpleName().substring(6);
	}
	
	public void addAspect(Class<? extends InitiumAspect> aspectClass)
	{
		String aspectId = aspectClassToString(aspectClass);
		aspects.put(aspectId, (InitiumAspect)GameUtils.createObject("com.universeprojects.miniup.server.aspects.Aspect"+aspectId));
		@SuppressWarnings("unchecked")
		Set<String> aspectIds = (Set<String>)entity.getProperty("_aspects");
		aspectIds.add(aspectId);
		entity.setPropertyManually("_aspects", aspectIds);
	}
	
	public boolean isAspectPresent(Class<? extends InitiumAspect> aspectClass)
	{
		if (aspects==null) return false;
		
		return aspects.containsKey(aspectClassToString(aspectClass));
	}
	
	public boolean hasAspects()
	{
		if (aspects==null || aspects.isEmpty())
			return false;
		
		return true;
	}
	
	public Collection<InitiumAspect> getAspects()
	{
		return aspects.values();
	}

	public InitiumAspect getInitiumAspect(String aspectId)
	{
		if (aspects==null) return null;
		return aspects.get(aspectId);
	}

	@Override
	public String getSchemaKind()
	{
		return entity.getKind();
	}

	@Override
	public Key getKey()
	{
		return entity.getKey();
	}

	@Override
	public String getName()
	{
		return (String)entity.getProperty("name");
	}

	@Override
	public String getItemClass()
	{
		return (String)entity.getProperty("itemClass");
	}

	@Override
	public Long getQuantity()
	{
		return (Long)entity.getProperty("quantity");
	}

	@Override
	public void setQuantity(Long value)
	{
		entity.setProperty("quantity", value);
	}

	@Override
	public Long getDurability()
	{
		return (Long)entity.getProperty("durability");
	}

	@Override
	public void setDurability(Long value)
	{
		entity.setProperty("durability", value);
	}

	@Override
	public Object getProperty(String fieldName)
	{
		return entity.getProperty(fieldName);
	}

	@Override
	public void setProperty(String fieldName, Object value)
	{
		entity.setProperty(fieldName, value);
	}

	@Override
	public Collection<String> getPropertyNames()
	{
		return db.getFieldNamesForEntity(entity.getKind());
	}

	@Override
	public Collection<String> getAspectNames()
	{
		return (Collection<String>)entity.getProperty("_aspects");
	}

	@Override
	public GameAspect<Key> getAspect(String aspectName)
	{
		if (aspects==null) return null;
		return aspects.get(aspectName);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends InitiumAspect> T getAspect(Class<T> clazz)
	{
		return (T)getAspect(clazz.getSimpleName().substring(6));
	}

	@Override
	public boolean hasAspect(String aspectName)
	{
		if (aspects!=null)
			return aspects.containsKey(aspectName);
		
		return false;
	}
	
	public <T extends InitiumAspect> boolean hasAspect(Class<T> clazz)
	{
		return hasAspect(clazz.getSimpleName().substring(6));
	}


	public boolean isProcedural()
	{
		if (entity.getKey().isComplete()) return false;
		
		if (entity.getAttribute("proceduralKey")!=null) return true;
		
		return false;
	}

	
}
