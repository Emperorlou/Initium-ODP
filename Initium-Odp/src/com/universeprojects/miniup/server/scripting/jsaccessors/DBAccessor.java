package com.universeprojects.miniup.server.scripting.jsaccessors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query.FilterOperator;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.EntityPool;
import com.universeprojects.cacheddatastore.QueryHelper;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.OperationBase;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.scripting.events.GlobalEvent;
import com.universeprojects.miniup.server.scripting.events.ScriptEvent;
import com.universeprojects.miniup.server.scripting.wrappers.Buff;
import com.universeprojects.miniup.server.scripting.wrappers.EntityWrapper;
import com.universeprojects.miniup.server.scripting.wrappers.Character;
import com.universeprojects.miniup.server.scripting.wrappers.Discovery;
import com.universeprojects.miniup.server.scripting.wrappers.Item;
import com.universeprojects.miniup.server.scripting.wrappers.Location;
import com.universeprojects.miniup.server.scripting.wrappers.Path;
import com.universeprojects.miniup.server.services.CombatService;
import com.universeprojects.miniup.server.services.ScriptService;

/**
 * Wraps ODPDBAccess class in order to limit the available DBA methods and their functionality.
 * 
 * This class is an incomplete. For the time being, you would need to add wrappers for any other
 * methods you want to use from the DBA.
 * 
 * @author aboxoffoxes
 * @author spfiredrake
 *
 */
public class DBAccessor {
	private final ODPDBAccess db;
	private final HttpServletRequest request;
	private final CombatService cs;
	
	public final String[] EquipSlots = ODPDBAccess.EQUIPMENT_SLOTS;
	
	public DBAccessor(ODPDBAccess db, HttpServletRequest request)
	{
		this.db = db;
		this.request = request;
		this.cs = new CombatService(db);
	}
	
	public EntityWrapper getCurrentCharacter()
	{
		return ScriptService.wrapEntity(db.getCurrentCharacter(), db);
	}
	
	public boolean executeScript(ScriptEvent currentEvent, String scriptName, Object... entities)
	{
		List<CachedEntity> scripts = db.getFilteredList("Script", "name", scriptName);
		if(scripts.size() > 1)
			throw new RuntimeException("Duplicate scripts detected: " + scriptName);
		if(scripts.size() == 0)
		{
			ScriptService.log.log(Level.WARNING, "Script with internal name " + scriptName + " not found.");
			return false;
		}
		CachedEntity script = scripts.get(0);
		if("global".equals(script.getProperty("type")) == false)
		{
			ScriptService.log.log(Level.SEVERE, "Cannot call non-global script through core.executeScript: " + scriptName);
			return false;
		}
		
		// Pass along the current character, if we have one.
		CachedEntity currentChar = null;
		if(currentEvent.currentCharacter != null) currentChar = currentEvent.currentCharacter.wrappedEntity;
		GlobalEvent event = new GlobalEvent(db, currentChar);
		event.addArguments(db, entities);
		ScriptService service = ScriptService.getScriptService(db);
		boolean executed = service.executeScript(event, script, (EntityWrapper)null);
		if(executed)
		{
			// Same thing, pass off the saving/deleting of entities to the original context. 
			for(EntityWrapper save:event.getSaveWrappers())
				currentEvent.saveEntity(save);
			for(EntityWrapper delete:event.getDeleteWrappers())
				currentEvent.deleteEntity(delete);
			currentEvent.haltExecution |= event.haltExecution;
			currentEvent.reloadWidgets |= event.reloadWidgets;
			// Should check for null here...
			currentEvent.errorText += event.errorText;
			currentEvent.descriptionText += event.descriptionText;
			currentEvent.popupMessage += event.popupMessage;
		}
		return executed;
	}
	
	public Character getCharacterByKey(Key characterKey)
	{
		CachedEntity ent = db.getEntity(characterKey);
		if(ent == null) return null;
		return new Character(ent, db);
	}
	
	public Character getCharacterById(Long charId)
	{
		CachedEntity ent = db.getEntity("Character", charId);
		if(ent == null) return null;
		return new Character(ent, db);
	}
	
	public Character getCharacterByName(String characterName)
	{
		CachedEntity ent = db.getCharacterByName(characterName);
		if(ent == null) return null;
		return new Character(ent, db);
	}
	
	public Character[] getCharactersByName(String characterName)
	{
		List<CachedEntity> ents = db.getCharactersByName(characterName);
		if(ents == null) return new Character[0];
		
		List<Character> chars = new ArrayList<Character>();
		for(CachedEntity charEnt:ents)
		{
			if(charEnt != null)
				chars.add(new Character(charEnt, db));
		}
		
		Character[] retChars = new Character[chars.size()];
		return chars.toArray(retChars);
	}
	
	public Location getLocationByKey(Key locationKey)
	{
		CachedEntity ent = db.getEntity(locationKey);
		if(ent == null) return null;
		return new Location(ent, db);
	}
	
	public Location getLocationById(Long locationId)
	{
		CachedEntity ent = db.getEntity("Location", locationId);
		if(ent == null) return null;
		return new Location(ent, db);
	}
	
	public Location[] getLocationsByParentKey(Key locationKey)
	{
		List<Location> childLocs = new ArrayList<Location>();
		QueryHelper qh = new QueryHelper(db.getDB());
		List<CachedEntity> dbLocs = qh.getFilteredList("Location", 1000, null, "parentLocationKey", FilterOperator.EQUAL, locationKey);
		for(CachedEntity locEnt:dbLocs)
		{
			if(locEnt != null)
			{
				childLocs.add(new Location(locEnt, this.db));
			}
		}
		
		Location[] wrapped = new Location[childLocs.size()];
		return childLocs.toArray(wrapped);
	}
	
	public Location[] getLocationsByParentId(Long locationId)
	{
		return getLocationsByParentKey(getKey("Location", locationId));
	}
	
	public Item getItemByKey(Key itemKey)
	{
		CachedEntity ent = db.getEntity(itemKey);
		if(ent == null) return null;
		return new Item(ent, db);
	}
	
	public Item getItemById(Long itemId)
	{
		CachedEntity ent = db.getEntity("Item", itemId);
		if(ent == null) return null;
		return new Item(ent, db);
	}
	
	public Item[] getItemsByName(String itemName)
	{
		List<Item> foundItems = new ArrayList<Item>();
		QueryHelper qh = new QueryHelper(db.getDB());
		List<CachedEntity> dbItems = qh.getFilteredList("Item", 1000, null, "name", FilterOperator.EQUAL, itemName);
		for(CachedEntity itemEnt:dbItems)
		{
			if(itemEnt != null)
			{
				foundItems.add(new Item(itemEnt, this.db));
			}
		}
		
		Item[] wrapped = new Item[foundItems.size()];
		return foundItems.toArray(wrapped);
	}

	public Path getPathByKey(Key pathKey)
	{
		CachedEntity ent = db.getEntity(pathKey);
		if(ent == null) return null;
		return new Path(ent, db);
	}
	
	public Path getPathById(Long pathId)
	{
		CachedEntity ent = db.getEntity("Path", pathId);
		if(ent == null) return null;
		return new Path(ent, db);
	}
	
	public Key getKey(String kind, Long id)
	{
		return KeyFactory.createKey(kind, id);
	}
	
	/**
	 * Use this for comparing keys
	 * @param object1
	 * @param object1
	 * @return
	 */
	public boolean equals(Object object1, Object object1) {
		return GameUtils.equals(object1, object1);
	}

	public boolean setInternalName(EntityWrapper wrapper, String newInternalName)
	{
		if(wrapper.wrappedEntity.hasProperty("internalName") == false)
			return false;
		
		String oldInternalName = wrapper.getInternalName();
		
		if(oldInternalName == null || "".equals(oldInternalName))
			oldInternalName = "(null)";
		
		wrapper.wrappedEntity.setProperty("internalName", newInternalName);
		ScriptService.log.log(Level.WARNING, "Updating " + wrapper.getKeyName() + " internal name: " + oldInternalName + " to " + newInternalName);
		return true;
	}
	
	public EntityWrapper[] getEntities(String kind, Long... entityIds)
	{
		List<Key> entityKeys = new ArrayList<Key>();
		for(Long id:entityIds)
			entityKeys.add(KeyFactory.createKey(kind, id));
		
		List<CachedEntity> entities = db.getEntities(entityKeys);
		
		List<EntityWrapper> wrappedEntities = new ArrayList<EntityWrapper>();
		for(CachedEntity ent:entities)
		{
			if(ent != null)
				wrappedEntities.add(ScriptService.wrapEntity(ent, db));
		}
		
		EntityWrapper[] wrapped = new EntityWrapper[wrappedEntities.size()];
		return wrappedEntities.toArray(wrapped);
	}
	
	public boolean transferGold(Long amount, EntityWrapper fromCharacter, EntityWrapper toCharacter)
	{
		if(amount == null || amount < 0) return false;
		if(GameUtils.equals(fromCharacter.getKey(), toCharacter.getKey())) return false;
		Long fromCoins = (Long)fromCharacter.wrappedEntity.getProperty("dogecoins");
		if(fromCoins == null) fromCoins = 0L;
		
		if(fromCoins > 0)
		{
			Long toCoins = (Long)toCharacter.wrappedEntity.getProperty("dogecoins");
			if(toCoins == null) toCoins = 0L;
			
			Long transferAmount = amount;
			if(fromCoins < amount) transferAmount = fromCoins;
			
			fromCoins -= transferAmount;
			toCoins += transferAmount;
			fromCharacter.wrappedEntity.setProperty("dogecoins", fromCoins);
			toCharacter.wrappedEntity.setProperty("dogecoins", toCoins);
			return true;
		}
		return false;
	}
	
	public void moveItem(EntityWrapper currentCharacter, EntityWrapper item, EntityWrapper newContainer) throws UserErrorMessage 
	{
		db.doMoveItem(null, null, currentCharacter.wrappedEntity, item.wrappedEntity, newContainer.wrappedEntity);
	}
	
	public boolean destroyItem(EntityWrapper item, EntityWrapper currentCharacter, ScriptEvent currentEvent)
	{
		return destroyItem(item, currentCharacter, currentCharacter.getName()+"'s "+item.getName()+" was so badly damaged it has been destroyed.", currentEvent);
	}
	
	public boolean destroyItem(EntityWrapper item, EntityWrapper currentCharacter, String destroyMessage, ScriptEvent currentEvent)
	{
		unequipItem(item, currentCharacter);
		currentEvent.deleteEntity(item);
		
		if(destroyMessage != null && destroyMessage.length() > 0)
			currentEvent.sendGameMessage(currentCharacter, "<div class='equipment-destroyed-notice'>"+destroyMessage+"</div>");
		return true;
	}
	
	public boolean unequipItem(EntityWrapper item, EntityWrapper currentCharacter)
	{
		if(item == null || currentCharacter == null) return false;
		
		boolean removed = false;
		CachedEntity charEntity = currentCharacter.wrappedEntity;
		for (String slot : ODPDBAccess.EQUIPMENT_SLOTS)
		{
			if (charEntity.getProperty("equipment" + slot) != null 
					&& GameUtils.equals(item.getKey(), charEntity.getProperty("equipment"+slot)))
			{
				charEntity.setProperty("equipment" + slot, null);
				removed = true;
			}
		}
		
		return removed;
	}
	
	/**
	 * Creates a blank entity to work with in script context. This is to be used with
	 * caution, and should ONLY be used when creating an item (not modifying existing
	 * items). If uniqueName is omitted, will allow duplicates to be created.
	 * @param kind Only "Item", "Location" and "Path" are currently allowed.
	 * @param uniqueName Distinct name, to ensure only 1 of this item EVER gets created.
	 * @return The EntityWrapper item we want to create. Keep in mind that the only way
	 * to get the raw entity in script context is if it's a new entity.
	 */
	public EntityWrapper createEntity(String kind, String uniqueName)
	{
		if(!Arrays.asList("Item","Location","Path").contains(kind))
			throw new RuntimeException("Invalid kind specified for CachedEntity!");
		
		CachedDatastoreService ds = db.getDB();
		if((uniqueName == null || uniqueName.isEmpty())==false)
		{
			List<CachedEntity> entList = db.getFilteredList(kind, "name", uniqueName);
			if(!entList.isEmpty())
			{
				ScriptService.log.log(Level.SEVERE, "Entity " + kind + " with unique name " + uniqueName + " already exists!");
				throw new RuntimeException("Unable to create new entity!");
			}
		}
		
		CachedEntity newEnt = new CachedEntity(kind, ds);
		newEnt.setProperty("name", uniqueName);
		EntityWrapper wrapped = ScriptService.wrapEntity(newEnt, db);
		wrapped.isNewEntity = true;
		return wrapped;
	}
	
	/*################# CREATE MONSTER ###################*/
	public EntityWrapper createMonsterFromId(Long defID, Location location)
	{
		CachedEntity defEntity = db.getEntity("NPCDef", defID);
		if(defEntity == null) return null;
		return createMonsterInternal(defEntity, location);
	}
	
	public EntityWrapper createMonsterFromDef(String defName, Location location)
	{
		List<CachedEntity> defList = db.getFilteredList("NPCDef", "name", defName);
		if(defList.size() != 1)
		{
			// <defKind>.name must be unique
			return null; 
		}
		
		return createMonsterInternal(defList.get(0), location);
	}
	
	private EntityWrapper createMonsterInternal(CachedEntity npcDef, Location location)
	{
		Key locationKey = null;
		if(location != null) locationKey = location.getKey(); 
		CachedEntity monster = db.doCreateMonster(npcDef, locationKey);
		EntityWrapper newMonster = ScriptService.wrapEntity(monster, db);
		newMonster.isNewEntity = true;
		return newMonster;
	}
	
	/*################# CREATE ITEM ###################*/
	public EntityWrapper createItemFromId(Long defID)
	{
		CachedEntity defEntity = db.getEntity("ItemDef", defID);
		if(defEntity == null) return null;
		return createItemInternal(defEntity);
	}
	
	public EntityWrapper createItemFromDef(String defName)
	{
		List<CachedEntity> defList = db.getFilteredList("ItemDef", "name", defName);
		if(defList.size() != 1)
		{
			// <defKind>.name must be unique
			return null; 
		}
		
		return createItemInternal(defList.get(0));
	}
	
	public EntityWrapper createCharacterItem(Long defID, Character character) {
		Item newItem = (Item) createItemFromId(defID);
		newItem.setContainer(character);
		newItem.wrappedEntity.setProperty("hardcoreMode", character.isHardcore());
		
		return newItem;
	}
	public EntityWrapper createCharacterItem(String defName, Character character) {
		Item newItem = (Item) createItemFromDef(defName);
		newItem.setContainer(character);
		newItem.wrappedEntity.setProperty("hardcoreMode", character.isHardcore());
		
		return newItem;
	}
	
	private EntityWrapper createItemInternal(CachedEntity itemDef)
	{
		CachedEntity item = db.generateNewObject(itemDef, "Item");
		item.setProperty("maxDurability", item.getProperty("durability"));
		EntityWrapper newItem = ScriptService.wrapEntity(item, db);
		newItem.isNewEntity = true;
		return newItem;
	}
	
	public Double[] getCharacterMax(Character character)
	{
		return db.getMaxCharacterStats(character.getKey());
	}
	
	public double getServerDayNight()
	{
		return GameUtils.getDayNight();
	}
	
	public boolean setHometownKey(EntityWrapper character, Long locationId)
	{
		if(character == null || "Character".equals(character.getKind()) == false) return false;
		
		// Get the entity, so we can make sure it's a valid location.
		CachedEntity location = db.getEntity("Location", locationId);
		if(location == null) return false;
		character.wrappedEntity.setProperty("homeTownKey", location.getKey());
		return true;
	}
	
	public EntityWrapper newDiscovery(Character character, Path path)
	{
		CachedEntity discovery = null;
		if (path.getKey().isComplete() && character.getKey().isComplete())
		{
			CachedEntity oldDiscovery = db.getDiscoveryByEntity(character.getKey(), path.getKey());
			if (oldDiscovery != null) discovery = oldDiscovery;
		}

		if(discovery==null)
		{
			discovery = new CachedEntity("Discovery");
			// Set the starting attributes
			discovery.setProperty("characterKey", character.getKey());
			discovery.setProperty("entityKey", path.getKey());
			discovery.setProperty("kind", path.getKind());
			discovery.setProperty("location1Key", path.getLocation1Key());
			discovery.setProperty("location2Key", path.getLocation2Key());
			discovery.setProperty("hidden", false);
			discovery.setProperty("createdDate", new Date());
		}
		
		return new Discovery(discovery, db);
	}
	
	/*################# COMBAT ###################*/
	/**
	 * Leaves combat, assuming the attacker and defender are in combat with
	 * each other. True return indicates that a db save occurred.
	 * @param attacker Attacking entity, typically the player
	 * @param defender Defending entity, typically an NPC, though possibly another player in PCA
	 * @return True if entities were in combat together and are no longer in combat. False otherwise.
	 */
	public boolean leaveCombat(Character attacker, Character defender)
	{
		CachedEntity attack = attacker.wrappedEntity;
		CachedEntity defend = defender.wrappedEntity;
		if(cs.isInCombatWith(attack, defend, null))
		{
			cs.leaveCombat(attack, defend);
			return !cs.isInCombat(attack);
		}
		return false;
	}
	
	/**
	 * Puts character entities into combat with each other. True return
	 * indicates a db save occurred. 
	 * @param attacker Attacking entity, typically the player
	 * @param defender Defending entity, typically an NPC, though possibly another player in PCA	 
	 * @return True if entities enter combat with each other. False otherwise.
	 */
	public boolean enterCombat(Character attacker, Character defender)
	{
		return enterCombat(attacker, defender, null);
	}
	
	/**
	 * Puts character entities into combat with each other, given a location. True return
	 * indicates a db save occurred. 
	 * @param attacker Attacking entity, typically the player
	 * @param defender Defending entity, typically an NPC, though possibly another player in PCA
	 * @param location Location where combat takes place. Necessary to determine auto attack.	 
	 * @return True if entities enter combat with each other. False otherwise.
	 */
	public boolean enterCombat(Character attacker, Character defender, Location location)
	{
		if(attacker.isInCombat()) return false;
		CachedEntity attack = attacker.wrappedEntity;
		CachedEntity defend = defender.wrappedEntity;
		CachedEntity locate = null;
		boolean autoAttack = false;
		boolean clearCombatant = false;
		
		if(location != null)
		{
			locate = location.wrappedEntity;
			autoAttack = CommonChecks.checkLocationIsInstance(locate);
			clearCombatant = CommonChecks.checkLocationIsCombatSite(locate);
		}
		
		cs.enterCombat(attack, defend, autoAttack);
		if(clearCombatant)
			defend.setProperty("combatant", null);
		
		return cs.isInCombatWith(attack, defend, locate);
	}
}
