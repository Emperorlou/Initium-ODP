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
 * Deny join group application command.
 * 
 * @author Atmostphear
 * 
 */
public class CommandGroupDenyJoinApplication extends Command
{

	/**
	 * Command to accept a player's application to join a group. The
	 * "applicantId" key is required in the parameters.
	 * 
	 * @param request
	 *            Server request
	 * @param response
	 *            Server response
	 */
	public CommandGroupDenyJoinApplication(final ODPDBAccess db,
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
		Key groupKey = (Key) character.getProperty("groupKey");
		Long applicantId = Long.valueOf(parameters.get("applicantId"))
				.longValue();
		CachedEntity applicant = db.getEntity("Character", applicantId);

		if (applicant == null)
		{
			throw new UserErrorMessage("Invalid character ID.");
		}

		if (!db.applicationAcceptOrDenyChecks(applicant, character, groupKey))
		{
			return;
		}

		applicant.setProperty("groupStatus", null);
		applicant.setProperty("groupKey", null);
		applicant.setProperty("groupStatus", null);

		db.discoverAllGroupPropertiesFor(ds, applicant);

		ds.put(applicant);
	}
}