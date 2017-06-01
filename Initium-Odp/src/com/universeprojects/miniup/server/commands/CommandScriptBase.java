package com.universeprojects.miniup.server.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.ODPDBAccess.ScriptType;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.scripting.events.ScriptEvent;
import com.universeprojects.miniup.server.services.CombatService;
import com.universeprojects.miniup.server.services.ContainerService;
import com.universeprojects.miniup.server.services.MainPageUpdateService;
import com.universeprojects.miniup.server.services.ScriptService;

/**
 * Provides a base command type for use with scripts. Performs the necessary
 * parameter and script validation prior to running each individual command,
 * allowing the implementing commands to interject when necessary.
 * 
 * @author spotupchik
 */
public abstract class CommandScriptBase extends Command {

	public CommandScriptBase(ODPDBAccess db, HttpServletRequest request,
			HttpServletResponse response) {
		super(db, request, response);
	}
	
	protected abstract void validateCharacterState(CachedEntity character) throws UserErrorMessage;
	
	protected abstract void validateScriptState(CachedEntity trigger, CachedEntity script) throws UserErrorMessage;
	
	protected abstract ScriptEvent generateEvent(CachedEntity character, CachedEntity trigger, CachedEntity script, Map<String, String> parameters) throws UserErrorMessage;
	
	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		// TODO Auto-generated method stub
		ODPDBAccess db = getDB();
		CachedEntity character = db.getCurrentCharacter();
		validateCharacterState(character);
		
		// Safely fetch script and item entities
		Long scriptId = tryParseId(parameters, "scriptId");
		CachedEntity scriptSource = null;
		
		// Get the entity for which the script will run
		Key entityKey = getSourceEntityKey(parameters);
		CachedEntity entitySource = db.getEntity(entityKey);

		//// SECURITY CHECKS
		if("Script".equals(entitySource.getKind()))
		{
			if(GameUtils.enumEquals(scriptSource.getProperty("type"), ScriptType.global)==false)
				throw new RuntimeException("Specified script is not a global type!");
		}
		else
		{
			// If it's not a Script entity, allow it to go through.
			@SuppressWarnings("unchecked")
			List<Key> sourceScriptKeys = (List<Key>)entitySource.getProperty("scripts");
			if(sourceScriptKeys == null) sourceScriptKeys = new ArrayList<Key>();
			for(Key scriptKey:sourceScriptKeys)
			{
				if (GameUtils.equals(scriptId, scriptKey.getId()))
				{
					scriptSource = db.getEntity(scriptKey);
					break;
				}
			}
			if(scriptSource == null)
				throw new UserErrorMessage("The " + (entitySource != null ? entitySource.getKind() : "entity") + " does not have this effect!");
		}
		
		// Can player trigger this effect...
		switch(entitySource.getKind())
		{

			case("Script"):
			{
				// ...as a global script. Script is responsible for validating "permission" to execute.
				if(scriptSource != null)
					throw new RuntimeException("CommandExecuteScript: Invalid parameters specified!");
			}
			case("Character"):
			{
				// ...by being its originator:
				if (GameUtils.equals(entityKey, character.getKey())==false)
					throw new UserErrorMessage("You are not allowed to trigger others");
				break;
			}
			case("Item"):
			{
				// ...by being close enough to the item OR having it in their pocket
				Key itemContainerKey = (Key)entitySource.getProperty("containerKey");
				if (itemContainerKey==null || GameUtils.equals(character.getKey(), itemContainerKey)==false)
					throw new UserErrorMessage("You can only trigger items in your posession!");
				break;
			}
			case("Location"):
			{
				ContainerService cs = new ContainerService(db);
				
				if(cs.checkContainerAccessAllowed(character, entitySource)==false)
					throw new UserErrorMessage("You are not located at the specified trigger location!");
				break;
			}
			default:
			{
				throw new RuntimeException("CommandExecuteScript: unhandled source entity type");
			}
		}
		// Perform any additional validation here and throw an error if it's invalid		
		validateScriptState(entitySource, scriptSource);
		
		// Implementing commands are responsible for their own event objects.
		ScriptEvent event = generateEvent(character, entitySource, scriptSource, parameters);
		try
		{
			ScriptService service = ScriptService.getScriptService(db);
			CachedDatastoreService ds = db.getDB();
			ds.beginBulkWriteMode();
			if(service.executeScript(event, scriptSource, entitySource))
			{
				if(!event.haltExecution)
				{
					for(CachedEntity saveEntity:event.getSaveEntities())
						ds.put(saveEntity);
					for(CachedEntity delEntity:event.getDeleteEntities())
						ds.delete(delEntity);
					
					ds.commitBulkWrite();
					
					this.mergeOperationUpdates(event);
					
					if(character != null && event.updatesGameState())
					{
						CachedEntity location = db.getEntity((Key)character.getProperty("locationKey"));
						CachedEntity user = db.getCurrentUser();
						MainPageUpdateService mpus = new MainPageUpdateService(db, user, character, location, this);
						CombatService cs = new CombatService(db);
						if(event.getJavascriptResponse()==JavascriptResponse.FullPageRefresh)
						{
							mpus.shortcut_fullPageUpdate(cs);
						}
						else if(event.reloadWidgets)
						{
							mpus.updateInBannerCharacterWidget();
							mpus.updateInBannerCombatantWidget();
						}
					}
					else
						setJavascriptResponse(event.getJavascriptResponse());
				}
				afterExecuteScript(db, event);
			}
		}
		catch(UserErrorMessage uem)
		{
			throw uem;
		}
		catch(Exception ex)
		{
			// First pass, we want to log the exception but still allow it to continue.
			ScriptService.log.log(Level.SEVERE, "Unexpected error in ExecuteScript!", ex);
		}
	}

	/**
	 * Loops through the different parameters passed in to get the source entity
	 * for the script, throwing an error if multiple IDs were passed in.
	 * @param parameters Parameter map passed in to the command
	 * @return Key of the script source entity
	 * @throws UserErrorMessage
	 */
	protected Key getSourceEntityKey(Map<String, String> parameters) throws UserErrorMessage
	{
		String entityKind = null;
		Long entityId = null;
		// We need to make sure only 1 entity is being passed in here
		int entryCount = 0;
		if(parameters.containsKey("itemId"))
		{
			entryCount++;
			entityKind = "Item";
			entityId = tryParseId(parameters, "itemId");
		}
		if(parameters.containsKey("characterId"))
		{
			entryCount++;
			entityKind = "Character";
			entityId = tryParseId(parameters, "characterId");
		}
		if(parameters.containsKey("locationId"))
		{
			entryCount++;
			entityKind = "Location";
			entityId = tryParseId(parameters, "locationId");
		}
		if(parameters.containsKey("globalId"))
		{
			// Entity is a global script. Entity itself is the global script. 
			// Ignore all other params, as only the current character is executed against it
			entityKind = "Script";
			entityId = tryParseId(parameters, "globalId");
			entryCount = 1;
		}
		
		if(entryCount == 0)
			throw new UserErrorMessage("No entity was specified for given effect.");
		if(entryCount > 1)
			throw new RuntimeException("Expected only 1 entity, got " + entryCount);
		
		return KeyFactory.createKey(entityKind, entityId);
	}

	/**
	 * Handles the output back to the ODP after execution of the script.
	 * Not necessary to override if using the base impl.
	 * @param db
	 * @param event
	 * @throws UserErrorMessage
	 */
	protected void afterExecuteScript(ODPDBAccess db, ScriptEvent event) throws UserErrorMessage
	{
		if(event.descriptionText != null && event.descriptionText.isEmpty() == false && db.getCurrentCharacter() != null)
		{
			db.sendGameMessage(getDS(), db.getCurrentCharacter(), event.descriptionText);
		}
		if(event.errorText != null && event.errorText.isEmpty() == false)
		{
			throw new UserErrorMessage(event.errorText);
		}
		if(event.popupMessage != null && event.popupMessage.isEmpty() == false)
		{
			setPopupMessage(event.popupMessage);
		}
	}
}
