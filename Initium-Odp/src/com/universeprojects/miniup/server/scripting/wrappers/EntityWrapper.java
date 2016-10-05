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
	
	public void removeScript(String scriptName)
	{
		List<CachedEntity> scriptList = db.getFilteredList("Script", "internalName", scriptName);
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
	
	public boolean hasScript(String scriptName)
	{
		List<CachedEntity> searchScripts = db.getFilteredList("Script", "internalName", scriptName);
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
}
