package com.universeprojects.miniup.server.scripting.wrappers;

import java.security.spec.DSAGenParameterSpec;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PropertyContainer;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.QueryHelper;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.services.ScriptService;

/**
 * Base wrapper class for CachedEntity objects, for use with the Scripting 
 * service. Keeping instance of DB so we can retrieve other entities within
 * the wrapped objects themselves.
 * 
 * @author spfiredrake
 */
public class EntityWrapper extends BaseWrapper
{
	protected ODPDBAccess db;
	public boolean isNewEntity = false;
	public boolean isEmbeddedEntity = false;
	
	public EntityWrapper(CachedEntity entity, ODPDBAccess db)
	{
		this.db = db;
		this.wrappedEntity = entity;
		this.isNewEntity = !entity.getKey().isComplete();
		this.isEmbeddedEntity = wrappedEntity.getAttribute("entity") != null;
	}
	
	public boolean hasCharges()
	{
		return getCharges() > 0;
	}
	
	public Long getCharges()
	{
		if(this.wrappedEntity.hasProperty("charges")==false) return -1l;
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
	
	public Buff[] getBuffs()
	{
		List<Buff> buffs = new ArrayList<Buff>();
		if(this.isEmbeddedEntity == false)
		{
			List<EmbeddedEntity> entityBuffs = db.getBuffsFor(this.wrappedEntity);
			for(EmbeddedEntity buff:entityBuffs)
			{
				if(GameUtils.equals(buff.getProperty("parentKey"), this.getKey()))
					buffs.add(new Buff(buff, this.db, this));
			}
		}
		return buffs.toArray(new Buff[buffs.size()]);
	}
	
	public Buff[] getBuffsOfType(String buffName)
	{
		List<Buff> buffs = new ArrayList<Buff>();
		if(this.isEmbeddedEntity == false)
		{
			List<EmbeddedEntity> entityBuffs = db.getBuffsFor(this.wrappedEntity);
			for(EmbeddedEntity buff:entityBuffs)
			{
				if(buff.getProperty("name").equals(buffName) && GameUtils.equals(buff.getProperty("parentKey"), this.getKey()))
					buffs.add(new Buff(buff, this.db, this));
			}
		}
		return buffs.toArray(new Buff[buffs.size()]);
	}
	
	public Buff addBuff(String buffDefName)
	{
		ScriptService.log.log(Level.INFO, "Adding BuffDef by name to " + this.getKeyName() + ": " + buffDefName);
		EmbeddedEntity newBuff = db.awardBuffByDef(buffDefName, this.getKey());
		if(newBuff != null)
		{
			Buff buff = new Buff(newBuff, db, this);
			buff.isNewEntity = true;
			return buff;
		}
		else
			ScriptService.log.log(Level.SEVERE, "Unable to create buff via BuffDef: " + buffDefName);
		return null;
	}
	
	public Buff addManualBuff(String icon, String name, String description, int durationInSeconds, String field1Name, String field1Effect,
			String field2Name, String field2Effect, String field3Name, String field3Effect, int maximumCount)
	{
		EmbeddedEntity newBuff = (EmbeddedEntity)db.awardBuff(null, this.getEntity(), icon, name, description, durationInSeconds, field1Name, field1Effect, field2Name, field2Effect, field3Name, field3Effect, maximumCount);
		if(newBuff != null)
		{
			Buff buff = new Buff(newBuff, db, this);
			buff.isNewEntity = true;
			return buff;
		}
		else
			ScriptService.log.log(Level.SEVERE, "Unable to create manual buff " + name + " for character " + this.getId());
		return null;
	}
	
	public String getKeyName()
	{
		return getKey().toString();
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

	public String getName() 
	{
		return (String) wrappedEntity.getProperty("name");
	}
	
	public String getInternalName()
	{
		String internalName = null;
		if(wrappedEntity.hasProperty("internalName"))
			internalName = (String)wrappedEntity.getProperty("internalName");
		return internalName;
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
		if(this.wrappedEntity.hasProperty("scripts")==false) return false;
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
		if(this.wrappedEntity.hasProperty("scripts")==false) return false;
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
	
	public boolean addScript(String scriptName)
	{
		if(this.wrappedEntity.hasProperty("scripts")==false)
		{
			ScriptService.log.log(Level.SEVERE, "Entity hasProperty(script) returned false for " + wrappedEntity.getKind());
			return false;
		}
		
		QueryHelper qh = new QueryHelper(db.getDB());
		List<Key> addScripts = qh.getFilteredList_Keys("Script", 1, "name", scriptName);
		if(addScripts.isEmpty())
		{
			ScriptService.log.log(Level.WARNING, "Script '" + scriptName + "' not found.");
			return false;
		}
		
		Key newScriptKey = addScripts.get(0);
		return this.addScriptKey(newScriptKey);
	}
	
	public boolean addScriptById(Long scriptId)
	{
		if(this.wrappedEntity.hasProperty("scripts")==false)
		{
			ScriptService.log.log(Level.SEVERE, "Entity hasProperty(script) returned false for " + wrappedEntity.getKind());
			return false;
		}
		
		return this.addScriptKey(KeyFactory.createKey("Script", scriptId));
	}
	
	@SuppressWarnings("unchecked")
	public boolean addScriptKey(Key scriptKey)
	{
		CachedEntity newScript = db.getEntity(scriptKey);
		if(this.wrappedEntity.hasProperty("scripts")==false)
		{
			ScriptService.log.log(Level.SEVERE, "Entity hasProperty(script) returned false for " + wrappedEntity.getKind());
			return false;
		}
		
		if(newScript != null)
		{
			List<Key> entityScripts = (List<Key>)this.getProperty("scripts");
			if(entityScripts == null) entityScripts = new ArrayList<Key>();
			for(Key script:entityScripts)
			{
				if(GameUtils.equals(script, scriptKey))
					return false;
			}
			entityScripts.add(scriptKey);
			this.setProperty("scripts", entityScripts);
			ScriptService.log.log(Level.WARNING, "Script '" + (String)newScript.getProperty("name") + "' [" + scriptKey.toString() + "] associated to entity " + wrappedEntity.getKey().toString());
			return true;
		}
		else
			ScriptService.log.log(Level.WARNING, "Script '" + scriptKey.toString() + "' not found.");
		
		return false;
	}
}
