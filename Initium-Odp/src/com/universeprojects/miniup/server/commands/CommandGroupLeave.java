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
 * Leave group command.
 * 
 * @author Atmostphear
 * 
 */
public class CommandGroupLeave extends Command
{

	/**
	 * Command to leave the group as the requesting character.
	 * 
	 * @param request
	 *            Server request
	 * @param response
	 *            Server response
	 */
	public CommandGroupLeave(final ODPDBAccess db,
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

		// Check if we're not in a group
		Key groupKey = (Key) character.getProperty("groupKey");
		CachedEntity group = db.getEntity(groupKey);
		if (group == null)
		{
			throw new UserErrorMessage("You are not currently in a group.");
		}

		// Check if we're in the applied status. If we are, then we can leave
		// the group right away
		if ("Applied".equals(character.getProperty("groupStatus")))
		{
			db.doLeaveGroup(ds, character);
			return;
		}

		// Make sure that you are not the creator of the group. If you are, you
		// cannot leave and must reassign the creator to someone else.
		if (((Key) group.getProperty("creatorKey")).getId() == character
				.getKey().getId())
		{
			throw new UserErrorMessage(
					"You cannot leave a player group which you are the creator, you have to assign someone else as the creator first.");
		}

		db.doLeaveGroup(ds, character);

		setPopupMessage("You have left your group.");
	}

}