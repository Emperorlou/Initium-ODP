package com.universeprojects.miniup.server.commands;

import java.util.Date;
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
 * Create group command.
 * 
 * @author Atmostphear
 */

public class CommandGroupCreate extends Command
{
	/**
	 * Max length of group name
	 */
	public static final int MAX_GROUP_NAME_LENGTH = 40;

	/**
	 * Command to create a group.
	 * 
	 * Parameters: groupName - The name of the group being created.
	 * 
	 * @param request
	 *            Server request
	 * @param response
	 *            Server response
	 */
	public CommandGroupCreate(final ODPDBAccess db,
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
		CachedEntity character = db.getCurrentCharacter();
		
		if(CommonChecks.checkCharacterIsZombie(character))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");
		
		String groupName = parameters.get("groupName");
		if (groupName == null)
		{
			return;
		}
		// Make sure the group name requested is valid
		groupName = groupName.trim();
		groupName = groupName.replace("  ", " ");
		groupName = groupName.replaceAll(",+", ",");
		groupName = groupName.replaceAll("'+", "'");

		if (groupName.length() < 1
				|| groupName.length() > MAX_GROUP_NAME_LENGTH
				|| !groupName.matches("[A-Za-z', ]+"))
		{
			throw new UserErrorMessage(
					"Group name must contain only letters and spaces, and must be between 1 and 40 characters long.");
		}

		// Check if we're already in a group, we cannot create a new group if
		// we're in a group already
		Key groupKey = (Key) character.getProperty("groupKey");
		CachedEntity group = db.getEntity(groupKey);
		if (group != null)
		{
			if ("Applied".equals(character.getProperty("groupStatus")))
			{
				throw new UserErrorMessage(
						"You have already applied to be in another player group and so you cannot create a new group at this time. If you wish to create a new player group, you will have to cancel your application.");
			} else
			{
				throw new UserErrorMessage(
						"You are already in a player group and so you cannot create another. If you wish to create a new player group, you will have to leave the one you are in first.");
			}
		}
		// Check if the group name is already in use
		if (db.getGroupByName(groupName) != null)
		{
			throw new UserErrorMessage("A group by the name of '" + groupName
					+ "' already exists. Please choose another name.");
		}

		// Now create the group
		group = new CachedEntity("Group");
		group.setProperty("createdDate", new Date());
		group.setProperty("creatorKey", character.getKey());
		group.setProperty("name", groupName);
		group.setProperty("applicationMode", "AcceptingApplications");

		ds.put(group);

		character.setProperty("groupKey", group.getKey());
		character.setProperty("groupStatus", "Admin");

		ds.put(character);

		setPopupMessage("Group created successfully.");

		addCallbackData("groupId", group.getId());
	}
}