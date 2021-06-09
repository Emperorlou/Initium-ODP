package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.HtmlComponents;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

/**
 * Toggle party join command.
 * 
 * @author SPFiredrake
 * 
 */
public class CommandTogglePartyJoins extends Command 
{
	/**
	 * Command to toggle party joins, enabling or disabling based on current character entity state
	 * 
	 * @param request
	 *            Server request
	 * @param response
	 *            Server response
	 */
	
	public CommandTogglePartyJoins(final ODPDBAccess db,
			final HttpServletRequest request, final HttpServletResponse response)
	{
		super(db, request, response);
	}
	
	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage 
	{
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();
		
		if(CommonChecks.checkCharacterIsZombie(character))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");
		
		boolean isEnabled = "TRUE".equals(character.getProperty("partyJoinsAllowed"));
		if (!isEnabled && "COMBAT".equals(character.getProperty("mode")))
		{
			throw new UserErrorMessage("You cannot join parties in combat.");
		}
		
		String newStatus = isEnabled ? "FALSE" : "TRUE";
		character.setProperty("partyJoinsAllowed", newStatus);
		ds.put(character);
		
		updateHtml("#"+parameters.get("buttonId"), HtmlComponents.generateTogglePartyJoin(character));
	}

}