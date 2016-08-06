package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

/**
 * Group member member make group creator command.
 * 
 * @author Atmostphear
 * 
 */
public class CommandGroupMemberMakeGroupCreator extends Command
{

	/**
	 * Command to make a group member the new creator of the group. The
	 * "characterId" key is required in the parameters, this is the character
	 * that will become the new creator.
	 * 
	 * @param request
	 *            Server request
	 * @param response
	 *            Server response
	 */
	public CommandGroupMemberMakeGroupCreator(final ODPDBAccess db,
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
		CachedEntity admin = db.getCurrentCharacter(request);
		Key groupKey = (Key) admin.getProperty("groupKey");
		CachedEntity group = db.getEntity(groupKey);
		Long characterId = tryParseId(parameters, "characterId");
		CachedEntity newCreator = db.getEntity(KeyFactory.createKey(
				"Character", characterId));

		if (newCreator == null)
		{
			throw new UserErrorMessage("Invalid character ID.");
		}

		if (((Key) admin.getProperty("groupKey")).getId() != ((Key) newCreator
				.getProperty("groupKey")).getId()
				|| ((Key) admin.getProperty("groupKey")).getId() != group
						.getKey().getId())
		{
			throw new UserErrorMessage(
					"The member you are trying to set as the group creator is not part of your group.");
		}
		if (!admin.getKey().equals(group.getProperty("creatorKey")))
		{
			throw new UserErrorMessage(
					"You are not the creator of your group and cannot perform this action.");
		}

		group.setProperty("creatorKey", newCreator.getKey());
		newCreator.setProperty("group", "Admin");

		ds.put(group);
		ds.put(newCreator);

		setPopupMessage(newCreator.getProperty("name")
				+ " has been set as the group creator!");
	}
}