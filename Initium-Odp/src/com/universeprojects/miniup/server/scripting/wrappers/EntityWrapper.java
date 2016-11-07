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
public abstract class EntityWrapper 
{
	public CachedEntity wrappedEntity;
	protected ODPDBAccess db;
	
	public EntityWrapper(CachedEntity entity, ODPDBAccess db)
	{
		this.db = db;
		this.wrappedEntity = entity;
	}

	protected Object getProperty(String propertyName)
	{
		return wrappedEntity.getProperty(propertyName);
	}
	
	protected void setProperty(String propertyName, Object propValue)
	{
		wrappedEntity.setProperty(propertyName, propValue);
	}
	
	public boolean hasCharges()
	{
		return getCharges() > 0;
	}
	
	public Long getCharges()
	{
		return wrappedEntity.hasProperty("charges") ? (Long)this.getProperty("charges") : -1;
	}

	public boolean adjustCharges(Long addCharges)
	{
		Long newCharges = getCharges();
		if(newCharges > -1)
		{
			newCharges += addCharges;
			this.setProperty("charges", addCharges);
			return true;
		}
		return false;
	}
	
	protected Key getKey()
	{
		return wrappedEntity.getKey();
	}
	
	public String getKind()
	{
		return wrappedEntity.getKind();
	}

	public String getName() {
		return (String) wrappedEntity.getProperty("name");
	}
	
	@SuppressWarnings("unchecked")
	public void removeScript(String scriptName)
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
		}
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
