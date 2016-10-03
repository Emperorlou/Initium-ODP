package com.universeprojects.miniup.server.scripting.wrappers;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class Character extends WrapperBase 
{
	public Character(CachedEntity character, ODPDBAccess db) 
	{
		super(character, db);
	}

	public boolean isMode(String mode) {
		return this.getMode().equals(mode);
	}

	public Long getDogecoins() {
		return (Long)this.getProperty("dogecoins");
	}

	public Long addDogecoins(Long dogecoins) throws UserErrorMessage 
	{
		Long curCoins = (Long)this.getProperty("dogecoins") + dogecoins;
		if(curCoins < 0) 
			throw new UserErrorMessage("Character does not have enough coins!");
		this.setProperty("dogecoins", curCoins);
		return curCoins;
	}

	public String getMode() {
		return (String) this.getProperty("mode");
	}

	public void setMode(String mode) {
		this.setProperty("mode", mode);
	}

	public Key getLocationKey() {
		return (Key) this.getProperty("locationKey");
	}

	public void setLocationKey(Key locationKey) {
		this.setProperty("locationKey", locationKey);
	}
}