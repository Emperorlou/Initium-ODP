package com.universeprojects.miniup.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.gefcommon.shared.elements.GameAspect;
import com.universeprojects.gefcommon.shared.elements.GameObject;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
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
				if (GameUtils.classExist("com.universeprojects.miniup.server.aspects.Aspect"+aspectId))
					aspects.put(aspectId, (InitiumAspect)GameUtils.createObject("com.universeprojects.miniup.server.aspects.Aspect"+aspectId, this));
		}
		else
		{
			this.aspects = null;
		}
	}
	
	public void initialize()
	{
		if (hasAspects())
			for(InitiumAspect aspect:getAspects())
				aspect.initialize();
	}
	
	public boolean update()
	{
		boolean changed = false;
		if (hasAspects())
			for(InitiumAspect aspect:getAspects())
			{
				if (aspect.update())
					changed = true;
			}
		return changed;
	}
	
	public static List<InitiumObject> wrap(ODPDBAccess db, List<CachedEntity> entities)
	{
		List<InitiumObject> result = new ArrayList<InitiumObject>();
		
		for(CachedEntity entity:entities)
		{
			if (entity==null) continue;
			result.add(new InitiumObject(db, entity));
		}
		
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
	
	public String getProceduralKey()
	{
		return (String)entity.getAttribute("proceduralKey");
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
	
	public Key getContainerKey()
	{
		return (Key)entity.getProperty("containerKey");
	}
	
	public void setContainerKey(Key newContainer)
	{
		entity.setProperty("containerKey", newContainer);
	}
	
	public Date getMovedTimestamp()
	{
		return (Date)entity.getProperty("movedTimestamp");
	}
	
	public void setMovedTimestamp()
	{
		entity.setProperty("movedTimestamp", new Date());
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

	private void moveItemTo(Key containerKey)
	{
		// TODO: Do some checks to make sure this is possible perhaps...
		
		
		// Remove it from the procedural map if necessary
		if (isProcedural())
		{
			GridMapService gms = db.getGridMapService();
			if (gms.isForLocation(containerKey)==false)
				throw new IllegalArgumentException("Unhandled situation. Unable to remove a procedural item from a location that the character isn't in (currently). This could change.");
			
			gms.removeProceduralEntity(getProceduralKey());
		}
		
		// Now put it on the character and set the move timestamp
		setContainerKey(containerKey);
		setMovedTimestamp();
	}
	
	public void moveItemToCharacter(CachedEntity character) throws UserErrorMessage
	{
		moveItemTo(character.getKey());
	}

	public void moveItemToContainer(CachedEntity containerItem) throws UserErrorMessage
	{
		moveItemTo(containerItem.getKey());
	}
	
	public void moveItemToLocation(CachedEntity location, Long tileX, Long tileY) throws UserErrorMessage
	{
		moveItemTo(location.getKey());
		
		GridMapService gms = db.getGridMapService();
		if (gms.isForLocation(location.getKey())==false)
			gms = new GridMapService(db, location);
		
		gms.setItemPosition(entity, tileX, tileY);
	}

	public Object getAttribute(String attributeKey)
	{
		return entity.getAttribute(attributeKey);
	}
	
	public void setAttribute(String attributeKey, Object value)
	{
		entity.setAttribute(attributeKey, value);
	}
	
	@Override
	public String toString()
	{
		return entity.toString();
	}

	public Long getMaxDurability()
	{
		return (Long)entity.getProperty("maxDurability");
	}

	public String getIcon()
	{
		return (String)entity.getProperty("icon");
	}

	public void setIcon(String imageUrl)
	{
		entity.setProperty("icon", imageUrl);
	}

	public void liveUpdateIcon(OperationBase command)
	{
		command.addJavascriptToResponse("updateItemImage("+getKey().getId()+", '"+GameUtils.getResourceUrl(getIcon())+"');");
	}

	@Override
	public Double getMass() {
		Long mass = (Long)entity.getProperty("weight");
		if (mass==null) mass = 0L;
		
		return mass.doubleValue();
	}

	@Override
	public void setMass(Double mass) {
		if (mass==null) mass = 0d;
		entity.setProperty("weight", mass.longValue());
	}

	@Override
	public Double getVolume() {
		Long space = (Long)entity.getProperty("space");
		if (space==null) space = 0L;
		
		return space.doubleValue();
	}

	@Override
	public void setVolume(Double volume) {
		if (volume==null) volume = 0d;
		
		entity.setProperty("space", volume.longValue());
	}

	@Override
	public GameAspect<Key> addAspect(String aspectName) {
		return aspects.put(aspectName, (InitiumAspect)GameUtils.createObject("com.universeprojects.miniup.server.aspects.Aspect"+aspectName, this));
	}

	@Override
	public void removeAspect(String aspectName) {
		aspects.remove(aspectName);
	}

	public void refetch(CachedDatastoreService ds) {
		entity.refetch(ds);
		
	}
}
