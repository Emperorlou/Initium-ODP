package com.universeprojects.miniup.server.commands;

import java.util.Map;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.CommonChecks;
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
		
		if(CommonChecks.checkCharacterIsZombie(character))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");
		
		Key locationKey = (Key)character.getProperty("locationKey");
		CachedEntity location = db.getEntity(locationKey);
		
		if (character.getProperty("userKey")==null)
			throw new UserErrorMessage("Unfortunately throwaway accounts cannot currently rename houses. It's weird I know.");
		
		// check if player is house owner
		if (!GameUtils.equals(location.getProperty("ownerKey"), character.getProperty("userKey")))
			throw new UserErrorMessage("You cannot rename a house you do not own.");
		
		String newName = parameters.get("newName");
		
		if (newName == null || newName == "")
			throw new UserErrorMessage("House name cannot be blank.");
		else if (newName.length() > 40)
			throw new UserErrorMessage("House name is too long. Max length is 40 characters.");
		else if (!newName.matches(ODPDBAccess.STORE_NAME_REGEX))
			throw new UserErrorMessage("House name can only have letters, numbers, commas, spaces, and common symbols.");
		
		// rename the location and edit location's description
		location.setProperty("name", newName);
		location.setProperty("description", "No one can go here unless they have the location shared with them. Feel free to store equipment and cash here!");
		
		ds.put(location);
		
		// rename the path button overlays
		List<CachedEntity> paths = db.getPathsByLocation(locationKey);
		
		for (CachedEntity path:paths) {
			if (GameUtils.equals(path.getProperty("location1Key"), locationKey)) {
				path.setProperty("location1ButtonNameOverride", "Go to " + newName);
				path.setProperty("location2ButtonNameOverride", "Leave " + newName);
			}
			else if (GameUtils.equals(path.getProperty("location2Key"), locationKey)) {
				path.setProperty("location2ButtonNameOverride", "Go to " + newName);
				path.setProperty("location1ButtonNameOverride", "Leave " + newName);
			}
			else
				throw new RuntimeException("Path from house to location was not found.");
		}
		
		ds.put(paths);
		
		setJavascriptResponse(JavascriptResponse.FullPageRefresh);
	}
}
