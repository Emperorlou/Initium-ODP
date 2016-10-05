package com.universeprojects.miniup.server.scripting.wrappers;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

/**
 * Scripting engine wrapper for the Character CachedEntity.
 * 
 * @author spfiredrake
 */
public class Character extends EntityWrapper 
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
	
	public Double getMaxHitpoints()
	{
		return (Double)this.getProperty("maxHitpoints");
	}
	
	public Double getHitpoints()
	{
		return (Double)this.getProperty("hitpoints");
	}
	
	public void setHitpoints(Double newHp)
	{
		this.setProperty("hitpoints", newHp);
	}
	
	/**
	 * Adds the specified HP to the characters current hit points, not exceeding maximum hitpoints.
	 * @param addHp The amount to adjust the characters hitpoints by. Can be negative.
	 * @return New HP amount.
	 */
	public Double addHitpoints(Double addHp)
	{
		return addHitpoints(addHp, false);
	}
	
	/**
	 * Adds the specified HP to the characters current hit points, not exceeding maximum hitpoints.
	 * @param addHp The amount to adjust the characters hitpoints by. Can be negative.
	 * @param overrideMax Indicates whether the adjustment can exceed maximum hitpoints.
	 * @return New HP amount.
	 */
	public Double addHitpoints(Double addHp, boolean overrideMax)
	{
		Double newHitpoints = Math.min(getHitpoints() + addHp, overrideMax ? Integer.MAX_VALUE : getMaxHitpoints());
		this.setProperty("hitpoints", newHitpoints);
		return newHitpoints;
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