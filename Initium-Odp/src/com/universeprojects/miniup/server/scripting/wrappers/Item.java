package com.universeprojects.miniup.server.scripting.wrappers;

import java.util.Date;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.ScriptService;

/**
 * Scripting engine wrapper for the Item CachedEntity.
 * 
 * @author spfiredrake
 */
public class Item extends EntityWrapper
{
	public boolean destroyed = false;
	protected EntityWrapper containerEntity;
	
	public Item(CachedEntity item, ODPDBAccess db)
	{
		super(item, db);
	}
	
	public Item(CachedEntity item, ODPDBAccess db, EntityWrapper container)
	{
		super(item, db);
		containerEntity = container;
	}
	
	public EntityWrapper container()
	{
		if(this.containerEntity == null)
		{
			CachedEntity parEnt = db.getEntity((Key)this.getProperty("containerKey"));
			if(parEnt != null)
				containerEntity = ScriptService.wrapEntity(parEnt, db);
		}
		return this.containerEntity;
	}

	public Key getContainerKey() {
		return (Key) this.getProperty("containerKey");
	}
	
	public boolean setContainer(EntityWrapper ent)
	{
		if(GameUtils.equals(getContainerKey(), ent.getKey()))
			return false;
		setProperty("containerKey", ent.getKey());
		setProperty("movedTimeStamp", new Date());
		return true;
	}
	
	public Long getDurability()
	{
		return (Long)this.getProperty("durability");
	}
	
	public Long getMaxDurability()
	{
		return (Long)this.getProperty("maxDurability");
	}
	
	/**
	 * Adjusts the durability by the specified amount.
	 * @param addDura Amount to add (or subtract) from durability.
	 * @return Whether the item needs to be destroyed.
	 */
	public boolean adjustDurability(Long addDura)
	{
		Long newDura = getDurability();
		Long maxDura = getMaxDurability();
		if(newDura == null) return false;
		if(maxDura == null) maxDura = newDura;
		newDura += addDura;
		this.setProperty("durability", Math.min(newDura, maxDura));
		return newDura < 0;
	}
	
	public boolean isKeyItem()
	{
		return this.getProperty("keyCode") != null;
	}
	
	public boolean isValidKeyCode(Long keyCode)
	{
		if(keyCode == null) return false;
		if(!isKeyItem()) return false;
		return GameUtils.equals(this.getProperty("keyCode"), keyCode);
	}
}
