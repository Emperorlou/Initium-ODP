package com.universeprojects.miniup.server.scripting.wrappers;

import java.util.logging.Level;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.services.ScriptService;

public class Path extends EntityWrapper 
{
	protected EntityWrapper location1Entity = null;
	protected EntityWrapper location2Entity = null;
	
	public Path(CachedEntity entity, ODPDBAccess db) {
		super(entity, db);
	}
	
	public Path(CachedEntity entity, ODPDBAccess db, EntityWrapper location1, EntityWrapper location2)
	{
		super(entity, db);
		location1Entity = location1;
		location2Entity = location2;
	}

	public Key getLocation1Key()
	{
		return (Key)this.getProperty("location1Key");
	}
	
	public Key getLocation2Key()
	{
		return (Key)this.getProperty("location2Key");
	}
	
	public EntityWrapper getLocation1Entity()
	{
		if(location1Entity == null)
			location1Entity = ScriptService.wrapEntity(db.getEntity(this.getLocation1Key()), db);
		
		return location1Entity;
	}
	
	public EntityWrapper getLocation2Entity()
	{
		if(location2Entity == null)
			location2Entity = ScriptService.wrapEntity(db.getEntity(this.getLocation2Key()), db);
		
		return location2Entity;
	}
	
	public boolean setLocation1Key(Key newKey)
	{
		Key oldKey = this.getLocation1Key();
		if(GameUtils.equals(oldKey, newKey)) return false;
		String oldKeyString = oldKey == null ? "(null)" : (oldKey.getKind() + "(" + oldKey.getId() + ")");
		String newKeyString = newKey == null ? "(null)" : (newKey.getKind() + "(" + newKey.getId() + ")");
		ScriptService.log.log(Level.WARNING, "Updating Path("+this.getId()+") location1Key from " + oldKeyString + " to " + newKeyString);
		this.setProperty("location1Key", newKey);
		return true;
	}
	
	public boolean setLocation2Key(Key newKey)
	{
		Key oldKey = this.getLocation2Key();
		if(GameUtils.equals(oldKey, newKey)) return false;
		String oldKeyString = oldKey == null ? "(null)" : (oldKey.getKind() + "(" + oldKey.getId() + ")");
		String newKeyString = newKey == null ? "(null)" : (newKey.getKind() + "(" + newKey.getId() + ")");
		ScriptService.log.log(Level.WARNING, "Updating Path("+this.getId()+") location1Key from " + oldKeyString + " to " + newKeyString);
		this.setProperty("location2Key", newKey);
		return false;
	}
	
	public boolean setLocation1(EntityWrapper newEntity)
	{
		Key newKey = newEntity == null ? null : newEntity.getKey();
		return setLocation1Key(newKey);
	}
	
	public boolean setLocation2(EntityWrapper newEntity)
	{
		Key newKey = newEntity == null ? null : newEntity.getKey();
		return setLocation2Key(newKey);
	}
	
	public Double getDiscoveryChance()
	{
		Double discoveryChance = (Double)this.getProperty("discoveryChance");
		if(discoveryChance == null) discoveryChance = -1.0;
		return discoveryChance;
	}
	
	public boolean setDiscoveryChance(Double newDiscovery)
	{
		if(GameUtils.equals(this.getDiscoveryChance(), newDiscovery)) return false;
		ScriptService.log.log(Level.WARNING, "Updating Path("+this.getId()+") discoveryChance to " + newDiscovery);
		return true;
	}
	
	public String getLocation1ButtonNameOverride()
	{
		String nameOverride = (String)this.getProperty("location1ButtonNameOverride");
		if(nameOverride != null && nameOverride.trim().length() == 0) nameOverride = null;
		return nameOverride;
	}
	
	public String getLocation2ButtonNameOverride()
	{
		String nameOverride = (String)this.getProperty("location2ButtonNameOverride");
		if(nameOverride != null && nameOverride.trim().length() == 0) nameOverride = null;
		return nameOverride;
	}
	
	public boolean setLocation1ButtonNameOverride(String newOverride)
	{
		String oldNameOverride = getLocation1ButtonNameOverride();
		if(GameUtils.equals(oldNameOverride, newOverride)) return false;
		this.setProperty("location1ButtonNameOverride", newOverride);
		ScriptService.log.log(Level.WARNING, "Updating Path("+this.getId()+") location1ButtonNameOverride from " + oldNameOverride + " to " + newOverride);
		return true;
	}
	
	public boolean setLocation2ButtonNameOverride(String newOverride)
	{
		String oldNameOverride = getLocation2ButtonNameOverride();
		if(GameUtils.equals(oldNameOverride, newOverride)) return false;
		this.setProperty("location2ButtonNameOverride", newOverride);
		ScriptService.log.log(Level.WARNING, "Updating Path("+this.getId()+") location2ButtonNameOverride from " + oldNameOverride + " to " + newOverride);
		return true;
	}
	
	public Long getLocation1LockCode()
	{
		return (Long)this.getProperty("location1LockCode");
	}
	
	public Long getLocation2LockCode()
	{
		return (Long)this.getProperty("location2LockCode");
	}
	
	public boolean setLocation1LockCode(Long newLockCode)
	{
		Long oldLockCode = getLocation1LockCode();
		if(GameUtils.equals(oldLockCode, newLockCode)) return false;
		this.setProperty("location1LockCode", newLockCode);
		ScriptService.log.log(Level.WARNING, "Updating Path("+this.getId()+") location1LockCode from " + oldLockCode + " to " + newLockCode);
		return true;
	}
	
	public boolean setLocation2LockCode(Long newLockCode)
	{
		Long oldLockCode = getLocation2LockCode();
		if(GameUtils.equals(oldLockCode, newLockCode)) return false;
		this.setProperty("location2LockCode", newLockCode);
		ScriptService.log.log(Level.WARNING, "Updating Path("+this.getId()+") location2LockCode from " + oldLockCode + " to " + newLockCode);
		return true;
	}
}
