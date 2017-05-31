package com.universeprojects.miniup.server.scripting.jsaccessors;

import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.QueryHelper;
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
	
	public boolean transferGold(Long amount, EntityWrapper fromCharacter, EntityWrapper toCharacter)
	{
		if(amount == null || amount < 0) return false;
		CachedEntity fromChar = ((BaseWrapper)fromCharacter).getEntity();
		CachedEntity toChar = ((BaseWrapper)toCharacter).getEntity();
		
		Long fromCoins = (Long)fromChar.getProperty("dogecoins");
		if(fromCoins == null) fromCoins = 0L;
		
		if(fromCoins > 0)
		{
			Long toCoins = (Long)toChar.getProperty("dogecoins");
			if(toCoins == null) toCoins = 0L;
			
			Long transferAmount = amount;
			if(fromCoins < amount) transferAmount = fromCoins;
			
			fromCoins -= transferAmount;
			toCoins += transferAmount;
			fromChar.setProperty("dogecoins", fromCoins);
			toChar.setProperty("dogecoins", toCoins);
			return true;
		}
		return false;
	}
	
	public void moveItem(EntityWrapper currentCharacter, EntityWrapper item, EntityWrapper newContainer) throws UserErrorMessage 
	{
		db.doMoveItem(null, ((BaseWrapper)currentCharacter).getEntity(), ((BaseWrapper)item).getEntity(), ((BaseWrapper)newContainer).getEntity());
	}
	
	public boolean destroyItem(EntityWrapper item, EntityWrapper currentCharacter)
	{
		db.doDestroyEquipment(null, ((BaseWrapper)currentCharacter).getEntity(), ((BaseWrapper)item).getEntity());
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
	
	public EntityWrapper createEntityFromDef(String defKind, String defName)
	{
		List<CachedEntity> defList = db.getFilteredList(defKind, "name", defName);
		if(defList.size() != 1)
		{
			// <defKind>.name must be unique
			return null; 
		}
		
		return createEntityInternal(defList.get(0));
	}
	
	public EntityWrapper createEntityFromID(String defKind, long defID)
	{
		CachedEntity defEntity = db.getEntity(defKind, defID);
		return createEntityInternal(defEntity);
	}
	
	private EntityWrapper createEntityInternal(CachedEntity def)
	{
		// Only Item or NPC are allowed (for now).
		if(def == null || Arrays.asList("ItemDef","NPCDef").contains(def.getKind())==false) return null;
		String entityKind = def.getKind().replace("Def", "").replace("NPC","Character");
		CachedEntity newObj = db.generateNewObject(def, entityKind);
		EntityWrapper ent = ScriptService.wrapEntity(newObj, db);
		ent.isNewEntity = true;
		return ent;
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
		((BaseWrapper)character).getEntity().setProperty("homeTownKey", location.getKey());
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
		CachedEntity attack = attacker.wrappedEntity;
		CachedEntity defend = defender.wrappedEntity;
		
		if(cs.isInCombat(attack)) return false;
		cs.enterCombat(attack, defend, false);
		
		return cs.isInCombatWith(attack, defend, null);
	}
}
