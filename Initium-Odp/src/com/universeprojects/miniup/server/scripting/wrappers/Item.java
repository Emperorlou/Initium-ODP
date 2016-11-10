package com.universeprojects.miniup.server.scripting.wrappers;

import java.util.Date;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
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
	public boolean destroyed = true;
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
	
	public Long getDurability()
	{
		return (Long)this.getProperty("durability");
	}
	
	public Long getMaxDurability()
	{
		return (Long)this.getProperty("maxDurability");
	}
	
	public Long adjustDurability(long addDura)
	{
		long newDura = getDurability();
		newDura += addDura;
		this.setProperty("charges", Math.min(newDura, getMaxDurability()));
		destroyed = newDura < 0;
		return newDura;
	}
}
