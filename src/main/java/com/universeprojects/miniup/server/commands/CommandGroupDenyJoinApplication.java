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
 * Deny join group application command.
 * 
 * @author Atmostphear
 * 
 */
public class CommandGroupDenyJoinApplication extends Command
{

	/**
	 * Command to deny a player's application to join a group.
	 * 
	 * Parameters: characterId - The character applying to the group.
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
		CachedEntity character = db.getCurrentCharacter();
		
		if(CommonChecks.checkCharacterIsZombie(character))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");
		
		Key groupKey = (Key) character.getProperty("groupKey");
		CachedEntity applicant = db.getCharacterById(tryParseId(parameters,
				"characterId"));

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
		
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
	}
}