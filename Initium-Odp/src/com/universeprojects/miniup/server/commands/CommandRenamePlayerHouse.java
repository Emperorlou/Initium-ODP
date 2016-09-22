package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.GameUtils;

/**
 * Allows the player to rename their house
 * 
 * Parameters:
 * 		newName - New name of the house
 * 
 * @author jDyn
 * 
 */
public class CommandRenamePlayerHouse extends Command {

	public CommandRenamePlayerHouse(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
	}
	
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();
		Key locationKey = (Key)character.getProperty("locationKey");
		CachedEntity location = db.getEntity(locationKey);
		
		// check if player is house owner
		if (!GameUtils.equals(location.getProperty("ownerKey"), character.getProperty("userKey")))
			throw new UserErrorMessage("You cannot rename a house you do not own.");
		
		String newName = parameters.get("newName");
		
		if (newName == null || newName == "")
			throw new UserErrorMessage("House name cannot be blank.");
		else if (newName.length() > 40)
			throw new UserErrorMessage("House name is too long. Max length is 40 characters.");
		else if (!newName.matches("[A-Za-z0-9 ,]+"))
			throw new UserErrorMessage("House name can only have letters, numbers, commas, and spaces in the name.");
		else
		{
			location.setProperty("name", newName);
			ds.put(location);
		}
	}
}
