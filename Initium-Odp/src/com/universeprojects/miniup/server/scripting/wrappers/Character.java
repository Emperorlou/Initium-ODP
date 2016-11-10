package com.universeprojects.miniup.server.scripting.wrappers;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.ODPDBAccess.CharacterMode;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.ScriptService;

/**
 * Scripting engine wrapper for the Character CachedEntity.
 * 
 * @author spfiredrake
 */
public class Character extends EntityWrapper 
{
	private List<Item> inventory = null;
	private Map<String, List<Item>> namedInventory = null;
	private Map<Key, Item> keyedInventory = null;
	private Map<String, Item> equippedInventory = null;
	
	public Character(CachedEntity character, ODPDBAccess db) 
	{
		super(character, db);
	}
	
	private List<Item> getInventory()
	{
		if(inventory == null)
		{
			populateInventory();
		}
		return inventory;
	}
	
	private Map<String, List<Item>> getNamedInventory()
	{
		if(namedInventory == null)
		{
			populateInventory();
		}
		return namedInventory;
	}
	
	private Map<Key, Item> getKeyedInventory()
	{
		if(keyedInventory == null)
		{
			populateInventory();
		}
		return keyedInventory;
	}
	
	private Map<String, Item> getEquippedInventory()
	{
		if(equippedInventory == null)
		{
			populateInventory();
		}
		return equippedInventory;
	}
	
	private void populateInventory()
	{
		inventory = new ArrayList<Item>();
		namedInventory = new HashMap<String, List<Item>>();
		keyedInventory = new HashMap<Key, Item>();
		equippedInventory = new HashMap<String, Item>();
		
		Map<Key, String> equipKeys = new HashMap<Key, String>();
		for(String slot:ODPDBAccess.EQUIPMENT_SLOTS)
		{
			Key equipSlot = (Key)wrappedEntity.getProperty("equipment" + slot);
			if(equipSlot != null)
				equipKeys.put(equipSlot, slot);
		}
			
		List<CachedEntity> items = db.getFilteredList("Item", "containerKey", this.getKey());
		for(CachedEntity item:items)
		{
			if(item != null)
			{
				Item newItem = new Item(item, db);
				String itemName = newItem.getName();
				if(!namedInventory.containsKey(itemName)) namedInventory.put(itemName, new ArrayList<Item>());
				namedInventory.get(itemName).add(newItem);
				keyedInventory.put(newItem.getKey(), newItem);
				if(equipKeys.containsKey(item.getKey()))
					equippedInventory.put(equipKeys.get(item.getKey()), newItem);
			}
		}
	}

	public boolean isMode(String mode) {
		return this.getMode().equals(mode);
	}

	public Long getDogecoins() {
		return (Long)this.getProperty("dogecoins");
	}

	public Long addDogecoins(Long dogecoins) throws UserErrorMessage 
	{
		return addDogecoins(dogecoins, false);
	}
	
	public Long addDogecoins(Long dogecoins, boolean takeLast) throws UserErrorMessage 
	{
		Long curCoins = (Long)this.getProperty("dogecoins") + dogecoins;
		if(takeLast && curCoins < 0) curCoins = 0L;
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
	
	public List<Buff> getBuffsOfType(String buffName)
	{
		List<CachedEntity> playerBuffs = db.getBuffsFor(this.getKey());
		List<Buff> buffs = new ArrayList<Buff>();
		for(CachedEntity buff:playerBuffs)
		{
			if(buff.getProperty("name").equals(buffName))
				buffs.add(new Buff(buff, this.db, this));
		}
		return buffs;
	}
	
	public Buff addBuff(String buffDefName)
	{
		ScriptService.log.log(Level.FINE, "Adding BuffDef by name: " + buffDefName);
		CachedEntity newBuff = db.awardBuffByDef(buffDefName, this.getKey());
		if(newBuff != null)
			return new Buff(newBuff, db, this);
		else
			ScriptService.log.log(Level.SEVERE, "Unable to create buff via BuffDef: " + buffDefName);
		return null;
	}
	
	public Buff addManualBuff(String icon, String name, String description, int durationInSeconds, String field1Name, String field1Effect,
			String field2Name, String field2Effect, String field3Name, String field3Effect, int maximumCount)
	{
		CachedEntity newBuff = db.awardBuff(null, this.getKey(), icon, name, description, durationInSeconds, field1Name, field1Effect, field2Name, field2Effect, field3Name, field3Effect, maximumCount);
		return new Buff(newBuff, db, this);
	}
	
	public String getType() 
	{
		return (String) this.getProperty("type");
	}

	public String getMode() 
	{
		return (String) this.getProperty("mode");
	}

	public boolean setMode(String mode) 
	{
		if(GameUtils.enumContains(CharacterMode.class, mode, false))
		{
			this.setProperty("mode", mode);
			return true;
		}
		return false;
	}
	
	public Location getLocation()
	{
		Key locKey = getLocationKey();
		CachedEntity location = db.getEntity(locKey);
		return new Location(location, db);
	}
	
	/**
	 * Sets the characters location to new specific location entity.
	 * @param newLocation Location WrappedEntity object. Will likely get this from core class
	 * @return Boolean indicating whether the character's location was changed
	 */
	public boolean setLocation(Location newLocation)
	{
		// TODO: Validate new location, or provide overload to force new location.
		// Reasons to force: teleportation, phasing, trap doors, etc.
		setLocationKey(newLocation.getKey());
		return true;
	}

	public Key getLocationKey() {
		return (Key) this.getProperty("locationKey");
	}

	public boolean setLocationKey(Key locationKey) {
		CachedEntity location = db.getEntity(locationKey);
		if(location == null) return false;
		this.setProperty("locationKey", locationKey);
		return true;
	}
	
	public boolean setLocationID(Long locationId)
	{
		Key locKey = KeyFactory.createKey("Location", locationId);
		return setLocationKey(locKey);
	}
	
	public List<Item> findInInventory(String itemName)
	{
		Map<String, List<Item>> invMap = getNamedInventory();
		if(invMap.containsKey(itemName))
			return invMap.get(itemName);
		return new ArrayList<Item>();
	}
	
	public boolean isItemEquipped(Item checkItem)
	{
		Map<String, Item> equipItems = getEquippedInventory();
		return equipItems.containsKey(checkItem.getKey());
	}
	
	public boolean isItemEquippedByName(String itemName)
	{
		Map<String, Item> equipItems = getEquippedInventory();
		for(Item item:equipItems.values())
		{
			if(item.getName().equalsIgnoreCase(itemName))
				return true;
		}
		return false;
	}
	
}