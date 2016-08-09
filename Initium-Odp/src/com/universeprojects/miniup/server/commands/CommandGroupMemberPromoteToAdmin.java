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
 * Group member promote to admin command.
 * 
 * @author Atmostphear
 * 
 */
public class CommandGroupMemberPromoteToAdmin extends Command
{

	/**
	 * Command to promote a member of a group to admin status.
	 * 
	 * Parameters: characterId - The group member being promoted to admin
	 * status.
	 * 
	 * @param request
	 *            Server request
	 * @param response
	 *            Server response
	 */
	public CommandGroupMemberPromoteToAdmin(final ODPDBAccess db,
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
		CachedEntity promoteCharacter = db.getCharacterById(tryParseId(
				parameters, "characterId"));

		if (promoteCharacter == null)
		{
			throw new UserErrorMessage("Invalid character ID.");
		}

		if (((Key) admin.getProperty("groupKey")).getId() != ((Key) promoteCharacter
				.getProperty("groupKey")).getId()
				|| ((Key) admin.getProperty("groupKey")).getId() != group
						.getKey().getId())
		{
			throw new UserErrorMessage(
					"The member you are trying to demote is not part of your group.");
		}
		if (admin.getKey().equals(group.getProperty("creatorKey")) == false)
		{
			throw new UserErrorMessage(
					"You are not the creator of your group and cannot perform this action.");
		}

		promoteCharacter.setProperty("groupStatus", "Admin");

		ds.put(promoteCharacter);

		setPopupMessage(promoteCharacter.getProperty("name")
				+ " has been promoted to admin!");
	}
}