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
 * Request to join group command.
 * 
 * @author Atmostphear
 * 
 */
public class CommandGroupRequestJoin extends Command
{

	/**
	 * Command to request to join a group.
	 * 
	 * Paramters: groupId - The desired group to join.
	 * 
	 * @param request
	 *            Server request
	 * @param response
	 *            Server response
	 */
	public CommandGroupRequestJoin(final ODPDBAccess db,
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
		String groupId = parameters.get("groupId");
		if (groupId == null)
		{
			throw new UserErrorMessage(
					"The group you are requesting to join does not exist.");
		}
		CachedEntity group = db.getGroupByName(groupId);

		// Checking if already in a group
		Key groupKey = (Key) character.getProperty("groupKey");
		CachedEntity currentGroup = db.getEntity(groupKey);
		if (currentGroup != null)
		{
			throw new UserErrorMessage(
					"You are already in a group and cannot join a new group until you leave your old one.");
		}

		// Check if group is not accepting applications
		String applicationMode = (String) group.getProperty("applicationMode");
		if (!"AcceptingApplications".equals(applicationMode))
		{
			throw new UserErrorMessage(
					"This group is not accepting new member applications at this time.");
		}

		// Finally, request to join the group
		character.setProperty("groupKey", group.getKey());
		character.setProperty("groupStatus", "Applied");
		character.setProperty("groupRank", null);

		ds.put(character);

		setPopupMessage("You have successfully applied to the "
				+ group.getProperty("name") + " group.");
	}

}