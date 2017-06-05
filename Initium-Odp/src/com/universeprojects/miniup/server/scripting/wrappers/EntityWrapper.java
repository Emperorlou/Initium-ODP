package com.universeprojects.miniup.server.scripting.wrappers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;

/**
 * Base wrapper class for CachedEntity objects, for use with the Scripting 
 * service. Keeping instance of DB so we can retrieve other entities within
 * the wrapped objects themselves.
 * 
 * @author spfiredrake
 */
public abstract class EntityWrapper extends BaseWrapper
{
	protected ODPDBAccess db;
	public boolean isNewEntity = false;
	
	public EntityWrapper(CachedEntity entity, ODPDBAccess db)
	{
		this.db = db;
		this.wrappedEntity = entity;
		this.isNewEntity = !entity.getKey().isComplete();
	}
	
	public boolean hasCharges()
	{
		return getCharges() > 0;
	}
	
	public Long getCharges()
	{
		Long charges = (Long)this.getProperty("charges");
		if(charges == null) charges = -1l;
		return charges;
	}
	
	public boolean adjustCharges(Long addCharges)
	{
		return adjustCharges(addCharges, false);
	}

	public boolean adjustCharges(Long addCharges, boolean allowEmpty)
	{
		Long newCharges = getCharges();
		// If it has charges, we can modify it. Otherwise, we explicitly
		// indicate allowing reload (allowEmpty) and must be adding charges.
		if(newCharges > 0 || (allowEmpty && newCharges > -1 && addCharges > 0))
		{
			newCharges += addCharges;
			this.setProperty("charges", Math.max(newCharges, 0));
			return true;
		}
		return false;
	}

	public Key getKey()
	{
		return wrappedEntity.getKey();
	}
	
	public Long getId()
	{
		return wrappedEntity.getId();
	}
	
	public String getKind()
	{
		return wrappedEntity.getKind();
	}

	public String getName() {
		return (String) wrappedEntity.getProperty("name");
	}
	
	public CachedEntity getEntity()
	{
		// Script context will only allow it if the entity is newly
		// created and hasn't been saved to DB yet.
		if(!isNewEntity)
			throw new RuntimeException("Security fault: Cannot access already saved raw entity!");
		
		return wrappedEntity;
	}
	
	@SuppressWarnings("unchecked")
	public boolean removeScript(String scriptName)
	{
		List<CachedEntity> scriptList = db.getFilteredList("Script", "name", scriptName);
		if(scriptList.size() > 0)
		{
			// Should only be a single script returned. Either way, get the first item.
			CachedEntity foundScript = scriptList.get(0);
			Key findKey = foundScript.getKey();
			List<Key> scriptKeys = (List<Key>)this.getProperty("scripts");
			Iterator<Key> keysIter = scriptKeys.iterator();
			Key current = null;
			boolean found = false;
			while(keysIter.hasNext())
			{
				current = keysIter.next();
				if(GameUtils.equals(current, findKey))
				{
					keysIter.remove();
					found = true;
					break;
				}
			}
			
			if(found)
				this.setProperty("scripts", scriptKeys);
			
			return found;
		}
		
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public boolean hasScript(String scriptName)
	{
		List<CachedEntity> searchScripts = db.getFilteredList("Script", "name", scriptName);
		List<Key> entityScripts = (List<Key>)this.getProperty("scripts");
		for(CachedEntity script:searchScripts)
		{
			Key scriptKey = script.getKey();
			for(Key entityScriptKey:entityScripts)
			{
				if(GameUtils.equals(scriptKey, entityScriptKey))
					return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public boolean addScript(String scriptName)
	{
		List<CachedEntity> addScripts = db.getFilteredList("Script", "name", scriptName);
		if(!addScripts.isEmpty())
		{
			CachedEntity newScript = addScripts.get(0);
			Key newScriptKey = newScript.getKey();
			List<Key> entityScripts = (List<Key>)this.getProperty("scripts");
			if(entityScripts == null) entityScripts = new ArrayList<Key>();
			for(Key script:entityScripts)
			{
				if(GameUtils.equals(script, newScriptKey))
					return false;
			}
			entityScripts.add(newScriptKey);
			this.setProperty("scripts", entityScripts);
			return true;
		}
		
		return false;
	}
}
