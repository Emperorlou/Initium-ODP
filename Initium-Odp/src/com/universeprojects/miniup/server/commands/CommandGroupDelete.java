package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

/**
 * Delete group command.
 * 
 * @author Atmostphear
 */

public class CommandGroupDelete extends Command
{

	/**
	 * Command to delete a group.
	 * 
	 * @param request
	 *            Server request
	 * @param response
	 *            Server response
	 */
	public CommandGroupDelete(final ODPDBAccess db,
			final HttpServletRequest request, final HttpServletResponse response)
	{
		super(db, request, response);
	}

	@Override
	public final void run(final Map<String, String> parameters)
			throws UserErrorMessage
	{
		// Variables
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter(request);
		Key groupKey = (Key) character.getProperty("groupKey");
		CachedEntity group = db.getEntity(groupKey);
		String groupName = (String) group.getProperty("name");

		// Deleting the group
		group.setProperty("createdDate", null);
		group.setProperty("creatorKey", null);
		group.setProperty("name", null);
		group.setProperty("applicationMode", null);

		ds.put(group);

		// Removing character from group
		db.doLeaveGroup(ds, character);

		ds.put(character);

		setPopupMessage(groupName + " has been deleted.");
	}
}