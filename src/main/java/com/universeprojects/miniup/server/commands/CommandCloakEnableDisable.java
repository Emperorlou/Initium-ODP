package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

/**
 * Enable disable cloak command.
 * 
 * @author Atmostphear
 * 
 */
public class CommandCloakEnableDisable extends Command
{

	/**
	 * Command to enable or disable cloaking.
	 * 
	 * @param request
	 *            Server request
	 * @param response
	 *            Server response
	 */
	public CommandCloakEnableDisable(final ODPDBAccess db,
			final HttpServletRequest request, final HttpServletResponse response)
	{
		super(db, request, response);
	}

	@Override
	public final void run(final Map<String, String> parameters)
			throws UserErrorMessage
	{
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();
		
		if(CommonChecks.checkCharacterIsZombie(character))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");

		if ("COMBAT".equals(character.getProperty("mode")))
		{
			throw new UserErrorMessage("You cannot cloak in combat.");
		}

		if (Boolean.TRUE.equals(character.getProperty("cloaked")))
		{
			character.setProperty("cloaked", false);
			
			addCallbackData("html", "<a onclick='toggleCloaked(event)'  title='Clicking here will show your equipment and stats to other players.'><img src='https://initium-resources.appspot.com/images/ui/cloakedDisabled.png' border=0/></a>");
		} 
		else
		{
			character.setProperty("cloaked", true);

			addCallbackData("html", "<a onclick='toggleCloaked(event)'  title='Clicking here will hide your equipment and stats from other players.'><img src='https://initium-resources.appspot.com/images/ui/cloakedEnabled.png' border=0/></a>");
		}

		ds.put(character);
	}
}