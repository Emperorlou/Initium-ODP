package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.commands.framework.Command.JavascriptResponse;

/**
 * Group member kick command.
 * 
 * @author Atmostphear
 * 
 */
public class CommandGroupMemberKick extends Command
{

	/**
	 * Command to kick a member of the group.
	 * 
	 * Parameters: "characterId" - The member being kicked from the group.
	 * 
	 * @param request
	 *            Server request
	 * @param response
	 *            Server response
	 */
	public CommandGroupMemberKick(final ODPDBAccess db,
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
		CachedEntity admin = db.getCurrentCharacter();
		Key groupKey = (Key) admin.getProperty("groupKey");
		CachedEntity group = db.getEntity(groupKey);
		CachedEntity kickCharacter = db.getCharacterById(tryParseId(parameters,
				"characterId"));

		if (kickCharacter == null)
		{
			throw new UserErrorMessage("Invalid character ID.");
		}

		if (((Key) admin.getProperty("groupKey")).getId() != ((Key) kickCharacter
				.getProperty("groupKey")).getId()
				|| ((Key) admin.getProperty("groupKey")).getId() != group
						.getKey().getId())
		{
			throw new UserErrorMessage(
					"The member you are trying to kick is not part of your group.");
		}

		if ("Admin".equals(admin.getProperty("groupStatus")) == false)
		{
			throw new UserErrorMessage(
					"You are not an admin of your group and cannot perform this action.");
		}

		if (GameUtils.equals((Key) group.getProperty("creatorKey"),
				(Key) kickCharacter.getKey()))
		{
			throw new UserErrorMessage("The creator cannot be kicked you fool.");
		}

		if ("Admin".equals(admin.getProperty("groupStatus")) == true 
                && "Admin".equals(kickCharacter.getProperty("groupStatus")) == true)
        {
            throw new UserErrorMessage(
                    "You can not kick a fellow admin.");
        }
		
		db.doLeaveGroup(ds, kickCharacter);

		setPopupMessage(kickCharacter.getProperty("name")
				+ " has been kicked from the group!");
		
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
	}
}