package com.universeprojects.miniup.server.scripting.jsaccessors;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.QueryHelper;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.NotificationType;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.scripting.events.GlobalEvent;
import com.universeprojects.miniup.server.scripting.events.ScriptEvent;
import com.universeprojects.miniup.server.scripting.wrappers.BaseWrapper;
import com.universeprojects.miniup.server.scripting.wrappers.EntityWrapper;
import com.universeprojects.miniup.server.scripting.wrappers.Character;
import com.universeprojects.miniup.server.scripting.wrappers.Item;
import com.universeprojects.miniup.server.scripting.wrappers.Location;
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
		ScriptService service = ScriptService.getScriptService(db);
		GlobalEvent event = new GlobalEvent(db, entities);
		boolean executed = service.executeScript(event, script, (EntityWrapper)null);
		if(executed)
		{
			// Same thing, pass off the saving/deleting of entities to the original context. 
			for(EntityWrapper save:event.getSaveWrappers())
				currentEvent.saveEntity(save);
			for(EntityWrapper delete:event.getDeleteWrappers())
				currentEvent.deleteEntity(delete);
			currentEvent.haltExecution |= event.haltExecution;
			currentEvent.errorText += event.errorText;
			currentEvent.descriptionText += event.descriptionText;
		}
		return executed;
	}
	
	public Character getCharacterByKey(Key characterKey)
	{
		return new Character(db.getEntity(characterKey), db);
	}
	
	public Character getCharacterById(Long charId)
	{
		return new Character(db.getEntity("Character", charId), db);
	}
	
	public Location getLocationByKey(Key locationKey)
	{
		return new Location(db.getEntity(locationKey), db);
	}
	
	public Location getLocationById(Long locationId)
	{
		return new Location(db.getEntity("Location", locationId), db);
	}
	
	public Item getItemByKey(Key itemKey)
	{
		return new Item(db.getEntity(itemKey), db);
	}
	
	public Item getItemById(Long itemId)
	{
		return new Item(db.getEntity("Item", itemId), db);
	}
	
	public List<EntityWrapper> getEntities(String kind, Long... entityIds)
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
		return wrappedEntities;
	}
	
	public boolean transferGold(Long amount, EntityWrapper fromCharacter, EntityWrapper toCharacter)
	{
		if(amount == null || amount < 0) return false;
		
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
		db.doMoveItem(null, currentCharacter.wrappedEntity, item.wrappedEntity, newContainer.wrappedEntity);
	}
	
	public boolean destroyItem(EntityWrapper item, EntityWrapper currentCharacter)
	{
		db.doDestroyEquipment(null, currentCharacter.wrappedEntity, item.wrappedEntity);
		return true;
	}
	
	/**
	 * Creates a blank entity to work with in script context. This is to be used with
	 * caution, and should ONLY be used when creating an item (not modifying existing
	 * items). If uniqueName is omitted, will allow duplicates to be created.
	 * @param kind Only "Item" and "Location" are currently allowed.
	 * @param uniqueName Distinct name, to ensure only 1 of this item EVER gets created.
	 * @return The EntityWrapper item we want to create. Keep in mind that the only way
	 * to get the raw entity in script context is if it's a new entity.
	 */
	public EntityWrapper createEntity(String kind, String uniqueName)
	{
		if(!Arrays.asList("Item","Location").contains(kind))
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
		EntityWrapper wrapped = ScriptService.wrapEntity(newEnt, db);
		wrapped.isNewEntity = true;
		return wrapped;
	}
	
	/*################# CREATE MONSTER ###################*/
	public EntityWrapper createMonsterFromID(Long defID, Location location)
	{
		CachedEntity defEntity = db.getEntity("NPCDef", defID);
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
	public EntityWrapper createItemFromID(Long defID)
	{
		CachedEntity defEntity = db.getEntity("ItemDef", defID);
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
	
	private EntityWrapper createItemInternal(CachedEntity itemDef)
	{
		CachedEntity item = db.generateNewObject(itemDef, "Item");
		item.setProperty("maxDurability", item.getProperty("durability"));
		EntityWrapper newItem = ScriptService.wrapEntity(item, db);
		newItem.isNewEntity = true;
		return newItem;
	}
	
	public double getServerDayNight()
	{
		return GameUtils.getDayNight();
	}
	
	public boolean setHometownKey(EntityWrapper character, Long locationId)
	{
		if(character == null || "Character".equals(character.getKind()) == false) return false;
		
		CachedEntity location = db.getEntity("Location", locationId);
		if(location == null) return false;
		character.wrappedEntity.setProperty("homeTownKey", location.getKey());
		return true;
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
		
		if(location != null)
		{
			locate = location.wrappedEntity;
			autoAttack = CommonChecks.checkLocationIsInstance(locate);
		}
		
		cs.enterCombat(attack, defend, autoAttack);
		return cs.isInCombatWith(attack, defend, locate);
	}
}
