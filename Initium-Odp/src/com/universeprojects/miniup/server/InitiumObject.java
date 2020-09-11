package com.universeprojects.miniup.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.gefcommon.shared.elements.GameAspect;
import com.universeprojects.gefcommon.shared.elements.GameObject;
import com.universeprojects.miniup.server.aspects.AspectPet.CommandPetFeed;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.GridMapService;

public class InitiumObject implements GameObject<Key>
{
	private GridMapService gms;
	final protected ODPDBAccess db;
	final protected CachedEntity entity;
	final protected EmbeddedEntity ee;
	final protected boolean isEmbedded;
	protected Map<String, InitiumAspect> aspects;
	
	@SuppressWarnings("unchecked")
	public InitiumObject(ODPDBAccess db, CachedEntity entity)
	{
		this.db = db;
		this.entity = entity;
		this.ee = null;
		this.isEmbedded = false;
		
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
	
	/**
	 * ONLY USE THIS WHEN ABSOLUTELY NECESSARY! If you need to access aspects that are on an embedded entity, use this constructor.
	 * Most of the parameters are absolutely useless when embedded, really we only care about the aspects. Everything but aspects will just
	 * return null.
	 * @param db
	 * @param entity - the embeddedentity that is wrapped by this initiumobject.
	 */
	@SuppressWarnings("unchecked")
	public InitiumObject(ODPDBAccess db, EmbeddedEntity ee) {
		this.db = db;
		this.entity = null;
		this.ee = ee;
		this.isEmbedded = true;
		
		Object aspectsObj = ee.getProperty("_aspects");
		
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
	
	public boolean isEmbedded() {
		return this.isEmbedded;
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
	
	public static List<InitiumObject> wrapEmbeddedEntities(ODPDBAccess db, List<EmbeddedEntity> entities){
		List<InitiumObject> result = new ArrayList<InitiumObject>();
		
		for(EmbeddedEntity ee:entities) {
			if(ee == null) continue;
			result.add(new InitiumObject(db, ee));
		}
		
		return result;
	}
	
	public EmbeddedEntity getEmbeddedEntity() {
		return ee;
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
	
	@SuppressWarnings("unchecked")
	public void addAspect(Class<? extends InitiumAspect> aspectClass)
	{
		String aspectId = aspectClassToString(aspectClass);
		aspects.put(aspectId, (InitiumAspect)GameUtils.createObject("com.universeprojects.miniup.server.aspects.Aspect"+aspectId));
		
		Set<String> aspectIds;
		
		aspectIds = (Set<String>) getProperty("_aspects");
		
		aspectIds.add(aspectId);
		
		setProperty("_aspects", aspectIds);
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

	/**
	 * Returns null if this object wraps an embedded entity.
	 */
	@Override
	public String getSchemaKind()
	{
		if(isEmbedded) return null;
		return entity.getKind();
	}

	@Override
	public Key getKey()
	{
		if(isEmbedded) return ee.getKey();
		return entity.getKey();
	}
	
	/**
	 * Returns null if this object wraps an embedded entity.
	 * @return
	 */
	public String getProceduralKey()
	{
		if(isEmbedded) return null;
		return (String)entity.getAttribute("proceduralKey");
	}

	@Override
	public String getName()
	{
		return (String)getProperty("name");
	}

	@Override
	public String getItemClass()
	{
		return (String)getProperty("itemClass");
	}

	@Override
	public Long getQuantity()
	{
		return (Long)getProperty("quantity");
	}

	@Override
	public void setQuantity(Long value)
	{
		setProperty("quantity", value);
	}

	@Override
	public Long getDurability()
	{
		return (Long)getProperty("durability");
	}

	@Override
	public void setDurability(Long value)
	{
		setProperty("durability", value);
	}
	
	public Key getContainerKey()
	{
		return (Key)getProperty("containerKey");
	}
	
	public void setContainerKey(Key newContainer)
	{
		setProperty("containerKey", newContainer);
	}
	
	public Date getMovedTimestamp()
	{
		return (Date)getProperty("movedTimestamp");
	}
	
	public void setMovedTimestamp()
	{
		setProperty("movedTimestamp", new Date());
	}

	@Override
	public Object getProperty(String fieldName)
	{
		if(isEmbedded) return ee.getProperty(fieldName);
		return entity.getProperty(fieldName);
	}

	@Override
	public void setProperty(String fieldName, Object value)
	{
		if(isEmbedded) ee.setProperty(fieldName, value);
		entity.setProperty(fieldName, value);
	}
	
	/**
	 * Returns null if this entity is embedded.
	 */
	@Override
	public Collection<String> getPropertyNames()
	{
		return db.getFieldNamesForEntity(getSchemaKind());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<String> getAspectNames()
	{
		return (Collection<String>)getProperty("_aspects");
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


	/**
	 * Returns false if the entity is embedded.
	 * @return
	 */
	public boolean isProcedural()
	{
=		if (entity.getKey().isComplete()) return false;
		
		if (getProceduralKey()!=null) return true;
		
		return false;
	}

	/**
	 * This method will do nothing if this object wraps an embeddedentity.
	 * @param containerKey
	 */
	private void moveItemTo(Key containerKey)
	{
		// TODO: Do some checks to make sure this is possible perhaps...
		
		if(isEmbedded) return;
		
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
	
	/**
	 * This method will do nothing if this object wraps an embeddedentity.
	 * @param character
	 * @throws UserErrorMessage
	 */
	public void moveItemToCharacter(CachedEntity character) throws UserErrorMessage
	{
		if(isEmbedded) return;
		moveItemTo(character.getKey());
	}

	/**
	 * This method will do nothing if this object wraps an embeddedentity.
	 * @param containerItem
	 * @throws UserErrorMessage
	 */
	public void moveItemToContainer(CachedEntity containerItem) throws UserErrorMessage
	{
		if(isEmbedded) return;
		moveItemTo(containerItem.getKey());
	}
	
	/**
	 * This method will do nothing if this object wraps an embeddedentity.
	 * @param location
	 * @param tileX
	 * @param tileY
	 * @throws UserErrorMessage
	 */
	public void moveItemToLocation(CachedEntity location, Long tileX, Long tileY) throws UserErrorMessage
	{
		if(isEmbedded) return;
		moveItemTo(location.getKey());
		
		GridMapService gms = db.getGridMapService();
		if (gms.isForLocation(location.getKey())==false)
			gms = new GridMapService(db, location);
		
		gms.setItemPosition(entity, tileX, tileY);
	}

	/**
	 * Returns null if this object wraps an embedded entity.
	 * @param attributeKey
	 * @return
	 */
	public Object getAttribute(String attributeKey)
	{
		if(isEmbedded) return null;
		return entity.getAttribute(attributeKey);
	}
	
	/**
	 * Does nothing if this object wraps an embedded entity.
	 * @param attributeKey
	 * @param value
	 */
	public void setAttribute(String attributeKey, Object value)
	{
		if(isEmbedded) return;
		entity.setAttribute(attributeKey, value);
	}
	
	@Override
	public String toString()
	{
		if(isEmbedded) return ee.toString();
		return entity.toString();
	}

	public Long getMaxDurability()
	{
		return (Long)getProperty("maxDurability");
	}

	public String getIcon()
	{
		return (String)getProperty("icon");
	}

	/**
	 * Does nothing if this object wraps an embeddedentity.
	 * @param imageUrl
	 */
	public void setIcon(String imageUrl)
	{
		setProperty("icon", imageUrl);
	}

	public void liveUpdateIcon(OperationBase command)
	{
		command.addJavascriptToResponse("updateItemImage("+getKey().getId()+", '"+GameUtils.getResourceUrl(getIcon())+"');");
	}
}
