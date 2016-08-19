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
		CachedEntity character = db.getCurrentCharacter(request);

		if ("COMBAT".equals(character.getProperty("mode")))
		{
			throw new UserErrorMessage("You cannot cloak in combat.");
		}

		if ((boolean) character.getProperty("cloaked") == false)
		{
			character.setProperty("cloaked", true);
			setPopupMessage("You are now cloaked.");
		} else if ((boolean) character.getProperty("cloaked") == true)
		{
			character.setProperty("cloaked", false);
			setPopupMessage("You are now uncloaked.");
		}

		ds.put(character);
	}
}