package com.universeprojects.miniup.server.scripting.wrappers;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.services.ScriptService;

public class Discovery extends Path 
{
	private EntityWrapper discoveryEntity = null;
	private Character characterEntity = null;
	public Discovery(CachedEntity entity, ODPDBAccess db) 
	{
		super(entity, db);
	}
	
	public Key getEntityKey()
	{
		return (Key)this.getProperty("entityKey");
	}
	
	public EntityWrapper getDiscoveryEntity()
	{
		if(discoveryEntity == null)
			discoveryEntity = ScriptService.wrapEntity(db.getEntity(this.getEntityKey()), db);
		
		return discoveryEntity;
	}
	
	public Key getCharacterKey()
	{
		return (Key)this.getProperty("characterKey");
	}
	
	public Character getCharacterEntity()
	{
		if(characterEntity == null)
			characterEntity = (Character)ScriptService.wrapEntity(db.getEntity(this.getCharacterKey()), db);
		
		return characterEntity;
	}
	
	public String getPathKind()
	{
		return (String)this.getProperty("kind");
	}
	
	public boolean isHidden()
	{
		return GameUtils.booleanEquals(this.getProperty("hidden"), true);
	}
	
	public boolean setHidden(boolean isHidden)
	{
		if(this.isHidden() ^ isHidden)
		{
			this.setProperty("hidden", isHidden);
			return true;
		}
		return false;
	}
}
