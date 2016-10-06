package com.universeprojects.miniup.server.scripting.wrappers;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.ScriptService;

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
	
	public List<Buff> getBuffs()
	{
		List<CachedEntity> playerBuffs = db.getBuffsFor(this.getKey());
		List<Buff> buffs = new ArrayList<Buff>();
		for(CachedEntity buff:playerBuffs)
			buffs.add(new Buff(buff, this.db, this));
		return buffs;
	}
	
	public Buff addBuff(String buffDefName)
	{
		// Get the BuffDef from the ODP. We will use that to create a new Buff, associate
		// it with the current character, then return back the Buff for save (since
		// we don't save in Script context).
		List<CachedEntity> buffDefs = db.getFilteredList("BuffDef", "name", buffDefName);
		if(buffDefs.size() > 1)
		{
			// D'oh! Log it, return back null. name should be unique.
			ScriptService.log.log(Level.WARNING, "BuffDef name not unique: " + buffDefName);
			return null;
		}
		
		// There should only be 1. If not, it will fall through and return a null value. 
		for(CachedEntity def:buffDefs)
		{
			CachedEntity newBuff = db.generateNewObject(def, "Buff");
			newBuff.setProperty("parentKey", this.getKey());
			return new Buff(newBuff, db, this); 
		}
		ScriptService.log.log(Level.INFO, "BuffDef name not found: " + buffDefName);
		return null;
	}
	
	public Buff addManualBuff(String icon, String name, String description, int durationInSeconds, String field1Name, String field1Effect,
			String field2Name, String field2Effect, String field3Name, String field3Effect, int maximumCount)
	{
		CachedEntity newBuff = db.awardBuff(null, this.getKey(), icon, name, description, durationInSeconds, field1Name, field1Effect, field2Name, field2Effect, field3Name, field3Effect, maximumCount);
		return new Buff(newBuff, db, this);
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