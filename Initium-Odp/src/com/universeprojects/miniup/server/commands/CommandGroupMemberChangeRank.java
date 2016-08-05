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
 * Group member change rank command.
 * 
 * @author Atmostphear
 * 
 */
public class CommandGroupMemberChangeRank extends Command
{

	/**
	 * Command to change the rank of a group member. The "rank" key is required
	 * in the parameters, this is the new rank of the group member. The
	 * "characterId" key is required in the parameters, this is the character
	 * that will be given a new rank.
	 * 
	 * @param request
	 *            Server request
	 * @param response
	 *            Server response
	 */
	public CommandGroupMemberChangeRank(final ODPDBAccess db,
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
		String rank = parameters.get("rank");
		CachedEntity admin = db.getCurrentCharacter(request);
		Key groupKey = (Key) admin.getProperty("groupKey");
		CachedEntity group = db.getEntity(groupKey);
		Long characterId = Long.valueOf(parameters.get("characterId"))
				.longValue();
		CachedEntity character = db.getEntity("Character", characterId);

		if (character == null)
		{
			throw new UserErrorMessage("Invalid character ID.");
		}

		if (((Key) admin.getProperty("groupKey")).getId() != ((Key) character
				.getProperty("groupKey")).getId()
				|| ((Key) admin.getProperty("groupKey")).getId() != group
						.getKey().getId())
		{
			throw new UserErrorMessage(
					"The member you are editing is not part of your group.");
		}

		if (!"Admin".equals(admin.getProperty("groupStatus")))
		{
			throw new UserErrorMessage(
					"You are not an admin of your group and cannot perform this action");
		}

		if (!rank.matches(db.GROUP_NAME_REGEX))
		{
			throw new UserErrorMessage(
					"You can only use letters, spaces, commas, and apostophes");
		}

		rank = rank.replace("'", "`");

		character.setProperty("groupRank", rank);

		ds.put(character);
	}
}