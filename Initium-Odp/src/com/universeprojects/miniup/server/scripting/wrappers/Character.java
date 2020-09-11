package com.universeprojects.miniup.server.scripting.wrappers;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.ODPDBAccess.CharacterMode;
import com.universeprojects.miniup.server.ODPDBAccess.GroupStatus;
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
	private List<Character> carriedCharacters = null;
	private Group characterGroup = null;
	
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
		inventory = inventory == null ? new ArrayList<Item>() : inventory;
		namedInventory = namedInventory == null ? new HashMap<String, List<Item>>() : namedInventory;
		keyedInventory = keyedInventory == null ? new HashMap<Key, Item>() : keyedInventory;
		equippedInventory = equippedInventory == null ? new HashMap<String, Item>() : equippedInventory;
		
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
				inventory.add(newItem);
				if(!namedInventory.containsKey(itemName)) namedInventory.put(itemName, new ArrayList<Item>());
				namedInventory.get(itemName).add(newItem);
				keyedInventory.put(newItem.getKey(), newItem);
				if(equipKeys.containsKey(item.getKey()))
				{
					equippedInventory.put(equipKeys.get(item.getKey()), newItem);
					equipKeys.remove(item.getKey());
				}
			}
		}
		
		if(equipKeys.isEmpty()==false)
		{
			// Ghosted/destroyed gear.
			List<Key> remainingKeys = new ArrayList<Key>(equipKeys.keySet());
			items = db.getEntities(remainingKeys);
			for(int i = 0; i < remainingKeys.size(); i++)
			{
				Key curKey = remainingKeys.get(i);
				// If item exists, it's ghosted, add to equippedInventory.
				// Otherwise, remove the key.
				if(items.get(i) != null)
					equippedInventory.put(equipKeys.get(curKey), new Item(items.get(i), db));
				else
					this.setProperty("equip" + equipKeys.get(curKey), null);
			}
		}
	}
	
	private void populateCarriedCharacters()
	{
		List<CachedEntity> chars = db.getFilteredList("Character", "containerKey", this.getKey());
		carriedCharacters = carriedCharacters == null ? new ArrayList<Character>() : carriedCharacters;
		for(CachedEntity carried:chars)
		{
			if(carried != null)
			{
				Character newChar = new Character(carried, db);
				carriedCharacters.add(newChar);
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
		Double newHitpoints = Math.min(getHitpoints() + addHp, overrideMax ? 100 : getMaxHitpoints());
		this.setProperty("hitpoints", newHitpoints);
		return newHitpoints;
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
	
	public EntityWrapper getLocation()
	{
		Key locKey = getLocationKey();
		CachedEntity location = db.getEntity(locKey);
		return ScriptService.wrapEntity(location, db);
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
		if(newLocation == null || GameUtils.equals(newLocation.getKey(), this.getLocationKey()))
			return false;
		this.setProperty("locationKey", newLocation.getKey());
		this.setProperty("locationEntryDatetime", new Date());
		return true;
	}

	public Key getLocationKey() {
		return (Key) this.getProperty("locationKey");
	}

	public boolean setLocationKey(Key locationKey) {
		Location location = (Location)ScriptService.wrapEntity(db.getEntity(locationKey), db);
		if(location == null) return false;
		return setLocation(location);
	}
	
	public boolean setLocationId(Long locationId)
	{
		Key locKey = KeyFactory.createKey("Location", locationId);
		return setLocationKey(locKey);
	}
	
	public Item[] getAllInventory()
	{
		List<Item> inv = getInventory();
		Item[] invItems = new Item[inv.size()];
		return inv.toArray(invItems);
	}
	
	public Item[] getAllEquipped()
	{
		List<Item> inv = new ArrayList<Item>(getEquippedInventory().values());
		Item[] invItems = new Item[inv.size()];
		return inv.toArray(invItems);
	}
	
	public Item[] findInInventory(String itemName)
	{
		Map<String, List<Item>> invMap = getNamedInventory();
		if(invMap.containsKey(itemName))
		{
			Item[] invItems = new Item[invMap.get(itemName).size()];
			return invMap.get(itemName).toArray(invItems);
		}
		return new Item[0];
	}
	
	public boolean isItemInInventory(Item checkItem)
	{
		Map<Key, Item> items = getKeyedInventory();
		for(Item item:items.values())
			if(GameUtils.equals(item.getKey(), checkItem.getKey()))
				return true;
		
		return false;
	}
	
	public boolean isItemEquipped(Item checkItem)
	{
		Map<String, Item> equipItems = getEquippedInventory();
		for(Item itm:equipItems.values())
			if(GameUtils.equals(itm.getKey(), checkItem.getKey()))
				return true;
		return false;
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
	
	public boolean isPlayerCharacter()
	{
		String pcType = (String)this.getProperty("type");
		return pcType == null || pcType.length() == 0 || "PC".equals(pcType);
	}
	
	public Item getEquipmentSlot(String slotName)
	{
		Map<String, Item> equipItems = getEquippedInventory();
		// Returns null if not found.
		return equipItems.get(slotName);
	}
	
	public Key getEquipmentKey(String slotName)
	{
		return (Key)this.getProperty("equipment" + slotName);
	}
	
	public boolean isInCombat()
	{
		return "COMBAT".equals(this.getProperty("mode"));
	}
	
	public boolean isInParty()
	{
		return GameUtils.isCharacterInParty(this.wrappedEntity);
	}
	
	public Character[] getParty()
	{
		if(isInParty()==false) return new Character[0];
		
		List<CachedEntity> getParty = db.getParty(db.getDB(), this.wrappedEntity);
		List<Character> partyChars = new ArrayList<Character>();
		for(CachedEntity pChar:getParty)
		{
			if(pChar != null)
				partyChars.add((Character)ScriptService.wrapEntity(pChar, db));
		}
		
		Character[] chars = new Character[partyChars.size()];
		return partyChars.toArray(chars);
	}
	
	public Character[] getCarriedCharacters()
	{
		if(carriedCharacters == null)
			populateCarriedCharacters();
		
		Character[] chars = new Character[carriedCharacters.size()];
		return carriedCharacters.toArray(chars);
	}
	
	public Map<String, String> getDamageMap()
	{
		return db.getValue_StringStringMap(this.wrappedEntity, "combatStatsDamageMap");
	}
	
	/*################# GROUPS ###################*/
	public Group getGroup()
	{
		if(characterGroup != null)
		{
			return characterGroup;
		}
		
		if(this.getProperty("groupKey")==null || GameUtils.enumEquals(this.getProperty("groupStatus"), GroupStatus.Applied))
			return null;
		
		CachedEntity group = db.getEntity((Key)this.getProperty("groupKey"));
		if(group != null)
		{
			characterGroup = new Group(group, db);
		}
		
		return characterGroup;
	}
}