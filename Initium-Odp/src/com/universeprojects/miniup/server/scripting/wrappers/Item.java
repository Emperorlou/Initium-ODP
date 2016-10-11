package com.universeprojects.miniup.server.scripting.wrappers;

import java.util.Date;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

/**
 * Scripting engine wrapper for the Item CachedEntity.
 * 
 * @author spfiredrake
 */
public class Item extends EntityWrapper
{
	public Item(CachedEntity item, ODPDBAccess db)
	{
		super(item, db);
	}

	public Key getContainerKey() {
		return (Key) this.getProperty("containerKey");
	}

	public void moveItem(EntityWrapper currentCharacter, EntityWrapper newContainer) throws UserErrorMessage 
	{
		db.doMoveItem(null, currentCharacter.wrappedEntity, this.wrappedEntity, newContainer.wrappedEntity);
	}
	
	public boolean hasCharges()
	{
		return getCharges() > 0;
	}
	
	public int getCharges()
	{
		return wrappedEntity.hasProperty("charges") ? (int)this.getProperty("charges") : -1;
	}
	
	public void setCharges(int numCharges)
	{
		this.setProperty("charges", numCharges);
	}
	
	public int adjustCharges(int numCharges)
	{
		int newCharges = getCharges();
		if(newCharges == -1)
			return -1;
		newCharges += numCharges;
		this.setProperty("charges", numCharges);
		return newCharges;
	}
}
