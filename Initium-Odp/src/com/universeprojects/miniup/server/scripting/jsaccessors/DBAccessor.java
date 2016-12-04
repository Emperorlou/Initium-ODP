package com.universeprojects.miniup.server.scripting.jsaccessors;

import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.scripting.events.GlobalEvent;
import com.universeprojects.miniup.server.scripting.events.ScriptEvent;
import com.universeprojects.miniup.server.scripting.wrappers.EntityWrapper;
import com.universeprojects.miniup.server.scripting.wrappers.Character;
import com.universeprojects.miniup.server.scripting.wrappers.Item;
import com.universeprojects.miniup.server.scripting.wrappers.Location;
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
	
	public DBAccessor(ODPDBAccess db, HttpServletRequest request)
	{
		this.db = db;
		this.request = request;
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
			ScriptService.log.log(Level.ALL, "Script with internal name " + scriptName + " not found.");
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
		CachedEntity fromChar = fromCharacter.wrappedEntity;
		CachedEntity toChar = toCharacter.wrappedEntity;
		
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
		db.doMoveItem(null, currentCharacter.wrappedEntity, item.wrappedEntity, newContainer.wrappedEntity);
	}
}
