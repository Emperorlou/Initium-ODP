package com.universeprojects.miniup.server.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.log.Log;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.scripting.events.SimpleEvent;
import com.universeprojects.miniup.server.scripting.events.ScriptEvent;
import com.universeprojects.miniup.server.services.ScriptService;

/**
 * Makes it possible to execute script associated with an item.
 * 
 * Keep in mind that this command is meant to be safe: if you add more variables to the script scope,
 * make sure to keep it secret, keep it safe.
 * 
 * Don't want to pass too much information into the command, as anyone viewing source
 * can see what params we're passing and try to game the system somehow. Only 1 of 3 entities
 * can be passed, since it works solely from the context of the entity in question.
 * Where we show/trigger the script, we should know the entity type, so set the param
 * accordingly there.
 * 
 * Parameters:
 * 		itemId - the ID of the item the script is attached to
 * 		characterId - the ID of the character the script is attached to
 * 		locationId - the ID of the location the script is attached to
 * 		scriptId - the ID of the script we will be running
 * 
 * @author aboxoffoxes
 * @author SPFiredrake
 *
 */
public class CommandExecuteScript extends Command {

	public CommandExecuteScript(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
	}
	
	private Key getSourceEntityKey(Map<String, String> parameters) throws UserErrorMessage
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
		
		if(entryCount == 0)
			throw new UserErrorMessage("Entity is not allowed to execute scripts this way.");
		if(entryCount > 1)
			throw new RuntimeException("Expected only 1 entity, got " + entryCount);
		
		return KeyFactory.createKey(entityKind, entityId);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage 
	{
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();
		
		// Safely fetch script and item entities
		Long scriptId = tryParseId(parameters, "scriptId");
		CachedEntity scriptSource = null;
		
		// Get the entity for which the script will run
		Key entityKey = getSourceEntityKey(parameters);
		CachedEntity entitySource = db.getEntity(entityKey);

		//// SECURITY CHECKS
		// Does this source have this script?
		List<Key> sourceScriptKeys = (List<Key>)entitySource.getProperty("scripts");
		for(Key scriptKey:sourceScriptKeys)
		{
			if (GameUtils.equals(scriptId, scriptKey.getId()))
			{
				scriptSource = db.getEntity(scriptKey);
				break;
			}
		}
		
		if(scriptSource == null)
			throw new UserErrorMessage("The entity does not have this effect!");

		// Can player trigger this effect...
		switch(entitySource.getKind())
		{
			// ...by being its originator:
			case("Character"):
			{
				if (GameUtils.equals(entityKey, character.getKey())==false)
				{
					throw new UserErrorMessage("You are not allowed to trigger others");
				}
			}
			case("Item"):
			{
				// ...by being close enough to the item OR having it in their pocket
				Key itemContainerKey = (Key)entitySource.getProperty("containerKey");
				if (itemContainerKey==null || GameUtils.equals(character.getKey(), itemContainerKey)==false)
				{
					throw new UserErrorMessage("You can only trigger items in your posession!");
				}
				break;
			}
			case("Location"):
			{
				if(db.checkContainerAccessAllowed(character, entitySource)==false)
					throw new UserErrorMessage("You are not located at the specified trigger location!");
			}
			default:
			{
				throw new RuntimeException("CommandExecuteScript: unhandled source entity type");
			}
		}

		
		try
		{
			ScriptService service = ScriptService.getScriptService(db);
			ScriptEvent event = new SimpleEvent(character, db);
			if(service.executeScript(event, scriptSource, entitySource))
			{
				if(!event.haltExecution)
				{
					for(CachedEntity saveEntity:event.getSaveEntities())
					{
						ds.put(saveEntity);
					}
					
					if(event.descriptionText != null && event.descriptionText != "")
					{
						insertHtmlBefore(".main-description", "<div class='main-description'>" + event.descriptionText + "</div>");
					}
				}
				else
				{
					if(event.errorText != null)
					{
						throw new UserErrorMessage(event.errorText);
					}
				}
			}
		}
		catch(UserErrorMessage uem)
		{
			throw uem;
		}
		catch(Exception ex)
		{
			// First pass, we want to log the exception but still allow it to continue.
			ScriptService.log.log(Level.ALL, "Unexpected error in ExecuteScript!", ex);
		}
	}
}
