package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.ODPDBAccess.CharacterMode;
import com.universeprojects.miniup.server.ODPDBAccess.ScriptType;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.scripting.events.SimpleEvent;
import com.universeprojects.miniup.server.scripting.events.ScriptEvent;

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
public class CommandScriptLink extends CommandScriptBase {

	public CommandScriptLink(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
	}
	
	@Override
	protected void validateCharacterState(CachedEntity character) throws UserErrorMessage
	{
		if(GameUtils.isPlayerIncapacitated(character))
			throw new UserErrorMessage("You are incapacitated and cannot trigger any effects!");
		if(GameUtils.enumEquals(character.getProperty("mode"), CharacterMode.COMBAT))
			throw new UserErrorMessage("You cannot trigger this effect in combat!");
	}
	
	@Override
	protected void validateScriptState(CachedEntity trigger, CachedEntity script) throws UserErrorMessage
	{
		switch(trigger.getKind())
		{
			case "Item":
				if(GameUtils.enumEquals(script.getProperty("type"), ScriptType.directItem) == false)
					throw new UserErrorMessage("Item does not support this effect.");
				break;
			case "Location":
				if(GameUtils.enumEquals(script.getProperty("type"), ScriptType.directLocation) == false)
					throw new UserErrorMessage("Location does not support this effect.");
				break;
			case "Character":
				// What sort of validation do we allow here? Will likely be directCharacter
				// script type, for <secret>magic</secret>. Just allow for now.
				break;
			default:
				throw new RuntimeException("Unexpected trigger source for script " + script.getProperty("name"));
		}
	}
	
	@Override
	protected ScriptEvent generateEvent(CachedEntity character, CachedEntity triggerEntity, 
			CachedEntity script, Map<String, String> parameters) throws UserErrorMessage
	{
		return new SimpleEvent(character, getDB());
	}

	@Override
	protected void processParameters(ScriptEvent event,
			Map<String, String> parameters) throws UserErrorMessage {
	}
}
