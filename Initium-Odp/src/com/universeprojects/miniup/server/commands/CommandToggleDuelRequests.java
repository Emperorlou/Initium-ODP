package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.HtmlComponents;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

/**
 * Toggle duel requests command
 * Note that this is a stub, for use when duel system gets re-implemented.
 * 
 * @author SPFiredrake
 * 
 */
public class CommandToggleDuelRequests extends Command 
{
	/**
	 * Command to toggle duel requests, enabling or disabling based on current character entity state
	 * 
	 * @param request
	 *            Server request
	 * @param response
	 *            Server response
	 */
	
	public CommandToggleDuelRequests(final ODPDBAccess db,
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
		// Make no changes, since Duel functionality is disabled for now. Just recreate the button.
		updateHtml(parameters.get("buttonId"), HtmlComponents.generateToggleDuel(character));
		throw new UserErrorMessage("Dueling has been disabled (and has been for months) because the current combat system doesn't work well with it. We will re-enable it once we have a solution.");
	}

}