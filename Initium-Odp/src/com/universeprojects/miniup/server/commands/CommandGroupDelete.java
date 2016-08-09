package com.universeprojects.miniup.server.commands;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
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

		// Error checking
		if (group == null)
		{
			throw new UserErrorMessage("You are not currently in a group.");
		}
		String groupName = (String) group.getProperty("name");
		List<CachedEntity> members = db.getGroupMembers(null, group);

		if (members.size() > 1)
		{
			throw new UserErrorMessage(
					"The group must have no members before it can be deleted.");
		}

		if (GameUtils.equals((Key) group.getProperty("creatorKey"),
				(Key) character.getKey()) == false)
		{
			throw new UserErrorMessage("Only the creator can delete the group.");
		}

		// Deleting the group
		ds.delete(group);

		db.doLeaveGroup(ds, character); // Sets group properties to null

		ds.put(character); // This is a bit redundant since it happens in
							// doLeaveGroup but I'll leave it anyways

		setPopupMessage(groupName + " has been deleted.");
	}
}