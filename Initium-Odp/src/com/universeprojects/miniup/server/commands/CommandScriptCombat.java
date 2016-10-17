package com.universeprojects.miniup.server.commands;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.ODPDBAccess.CharacterMode;
import com.universeprojects.miniup.server.ODPDBAccess.ScriptType;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.scripting.events.CombatEvent;
import com.universeprojects.miniup.server.scripting.events.ScriptEvent;

/**
 * Makes it possible to execute script during combat.
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
 * @author SPFiredrake
 *
 */
public class CommandScriptCombat extends CommandScriptBase {

	public CommandScriptCombat(ODPDBAccess db, HttpServletRequest request,
			HttpServletResponse response) {
		super(db, request, response);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void validateCharacterState(CachedEntity character)
			throws UserErrorMessage {
		if(GameUtils.enumEquals(character.getProperty("mode"), CharacterMode.COMBAT) == false)
			throw new UserErrorMessage("You can only trigger this effect in combat!");
	}
	
	@Override
	protected void validateScriptState(CachedEntity trigger, CachedEntity script) throws UserErrorMessage
	{
		if(GameUtils.enumEquals(script.getProperty("type"), ScriptType.combatItem) == false)
			throw new UserErrorMessage("You are unable to active this effect right now.");
	}

	@Override
	protected ScriptEvent generateEvent(CachedEntity character,
			CachedEntity trigger, CachedEntity script) throws UserErrorMessage {
		ODPDBAccess db = getDB();
		CachedEntity combatant = db.getCharacterCombatant(character);
		
		if(combatant == null) throw new RuntimeException("Character is in COMBAT mode, but combatant is null");
		return new CombatEvent(db, character, trigger, combatant);
	}

}
