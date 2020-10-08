package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

/**
 * Group member change description command.
 * 
 * @author Atmostphear
 * 
 */
public class CommandGroupChangeDescription extends Command
{

	/**
	 * Command to change the description of a group.
	 * 
	 * Parameters: description - The new description of the group.
	 * 
	 * @param request
	 *            Server request
	 * @param response
	 *            Server response
	 */
	public CommandGroupChangeDescription(final ODPDBAccess db,
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
		String description = parameters.get("description");
		CachedEntity admin = db.getCurrentCharacter();
		
		if(CommonChecks.checkCharacterIsZombie(admin))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");
		
		Key groupKey = (Key) admin.getProperty("groupKey");
		CachedEntity group = db.getEntity(groupKey);

		if (("Admin".equals(admin.getProperty("groupStatus"))) == false)
		{
			throw new UserErrorMessage(
					"You are not an admin of your group and cannot perform this action.");
		}

		if ((description.matches(db.STORE_NAME_REGEX)) == false)
		{
			throw new UserErrorMessage(
					"You can only use letters, numbers, spaces, commas, and apostophes.");
		}


		group.setProperty("description", description);

		ds.put(group);

		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
	}
}