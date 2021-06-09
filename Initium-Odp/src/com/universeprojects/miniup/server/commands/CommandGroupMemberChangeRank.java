package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.commands.framework.Command.JavascriptResponse;

/**
 * Group member change rank command.
 * 
 * @author Atmostphear
 * 
 */
public class CommandGroupMemberChangeRank extends Command
{

	/**
	 * Command to change the rank of a group member.
	 * 
	 * Parameters: rank - The new rank of the group member.
	 * 
	 * characterId - The group member receiving the new rank.
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
		CachedEntity admin = db.getCurrentCharacter();
		
		if(CommonChecks.checkCharacterIsZombie(admin))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");
		
		Key groupKey = (Key) admin.getProperty("groupKey");
		CachedEntity group = db.getEntity(groupKey);
		CachedEntity character = db.getCharacterById(tryParseId(parameters,
				"characterId"));

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
		
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
	}
}