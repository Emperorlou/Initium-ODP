package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPAuthenticator;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.MainPageUpdateService;

/**
 * Leaves a combat site taking the specified path and attempts to forget it.
 *  
 * @author spotupchik
 */
public class CommandLeaveAndForgetCombatSite extends Command {

	public CommandLeaveAndForgetCombatSite(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) 
	{
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException 
	{
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();
		CachedEntity location = ds.getIfExists((Key)character.getProperty("locationKey"));
		
		if(location == null || GameUtils.isContainedInList("CombatSite,CollectionSite",(String)location.getProperty("type"))==false)
			throw new UserErrorMessage("You can only leave and forget combat sites.");

		if (GameUtils.isPlayerIncapacitated(character))
			throw new UserErrorMessage("You cannot leave, you're incapacitated!");
		
		CachedEntity path = null;
		try
		{
			Long pathId = tryParseId(parameters, "pathId");
			path = db.getPathById(pathId);
		}
		catch (Exception ex)
		{
			throw new RuntimeException("Invalid pathId parameter specified.");
		}
		
		if(path == null)
			throw new UserErrorMessage("This path no longer exists.");
		
		// Throws relevant error messages, preventing subsequent code from executing.
		db.doCharacterTakePath(ds, character, path);
		
		try
		{
			db.doDeleteCombatSite(ds, character, location.getKey(), true, false);
		}
		catch(Exception ex)
		{
			// Just swallow the exception.
		}
		
		// Get the new location, so we can update using MPUS.
		location = ds.getIfExists((Key)character.getProperty("locationKey"));
		MainPageUpdateService mpus = new MainPageUpdateService(db, db.getCurrentUser(), character, location, this);
		mpus.updateFullPage_shortcut();
		if(location != null)
			db.sendGameMessage(ds, character, "You have arrived at " + location.getProperty("name") + ".");
		
	}

}
