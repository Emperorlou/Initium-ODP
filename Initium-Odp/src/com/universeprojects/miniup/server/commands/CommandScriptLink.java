package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.ODPDBAccess.CharacterMode;
import com.universeprojects.miniup.server.ODPDBAccess.ScriptType;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.scripting.events.SimpleEvent;
import com.universeprojects.miniup.server.scripting.actions.ScriptActionSimple;
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
public class CommandScriptLink extends Command {

	public CommandScriptLink(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException {
		ScriptActionSimple action = new ScriptActionSimple(db, this, parameters);
		
		action.execute();
	}
	
}
