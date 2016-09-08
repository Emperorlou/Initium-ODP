package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

/**
 * Enable disable party join command.
 * 
 * @author SPFiredrake
 * 
 */
public class CommandPartyJoinEnableDisable extends Command 
{
	/**
	 * Command to enable or disable party joins.
	 * 
	 * @param request
	 *            Server request
	 * @param response
	 *            Server response
	 */
	
	public CommandPartyJoinEnableDisable(final ODPDBAccess db,
			final HttpServletRequest request, final HttpServletResponse response)
	{
		super(db, request, response);
	}
	
	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage 
	{
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter(request);

		if (Boolean.TRUE.equals(character.getProperty("partyJoinsAllowed")))
		{
			character.setProperty("partyJoinsAllowed", false);
			
			addCallbackData("html", "<a onclick='togglePartyJoins(event)'  title='Clicking here will allow other players to join your party.'><img src='images/ui/partyJoinsDisallowed.png' border=0/></a>");
		} 
		else
		{
			if ("COMBAT".equals(character.getProperty("mode")))
			{
				throw new UserErrorMessage("You cannot join parties in combat.");
			}
			
			character.setProperty("partyJoinsAllowed", true);
			
			addCallbackData("html", "<a onclick='togglePartyJoins(event)'  title='Clicking here will disallow other players from joining your party.'><img src='images/ui/partyJoinsAllowed.png' border=0/></a>");
		}

		ds.put(character);
	}

}