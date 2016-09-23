package com.universeprojects.miniup.server.commands;

import java.util.List;
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

/**
 * Allows the player to delete their house
 * 
 * @author jDyn
 * 
 */
public class CommandDeletePlayerHouse extends Command {
	
	public CommandDeletePlayerHouse(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
	}
	
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		
		CachedEntity currentCharacter = db.getCurrentCharacter();
		Key currentLocationKey = (Key)currentCharacter.getProperty("locationKey");
		CachedEntity currentLocation = db.getEntity(currentLocationKey);
		
		// Check if current user is house owner
		if (!GameUtils.equals(currentLocation.getProperty("ownerKey"), currentCharacter.getProperty("userKey")))
			throw new UserErrorMessage("You cannot delete a house you do not own.");
		
		// Delete discovery entity associated with path between player house and town
		List<CachedEntity> paths = db.getPathsByLocation(currentLocationKey);
		
		for (CachedEntity path:paths) {
			if ("PlayerHouse".equals(path.getProperty("type"))) {
				ds.delete(db.getDiscoveryByEntity(currentCharacter.getKey(), path.getKey()));
				break;
			}
		}
		
		// Place character in closest town since house is "deleted" now
		currentCharacter.setProperty("locationKey", currentCharacter.getProperty("homeTownKey"));
		
		setJavascriptResponse(JavascriptResponse.FullPageRefresh);
	}
}