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
		
		CachedEntity character = db.getCurrentCharacter();
		CachedEntity path = db.getPathById(tryParseId(parameters, "pathId"));
		
		// Make sure path is a house path
		if (!"PlayerHouse".equals(path.getProperty("type")))
			throw new UserErrorMessage("The selected property is not a house and cannot be deleted.");
		
		// Check if player is at one end of the path they are trying to delete
		Object locationKey = character.getProperty("locationKey");
		Object otherLocationKey = null;
		
		if (GameUtils.equals(locationKey, path.getProperty("location1Key")))
			otherLocationKey = path.getProperty("location2Key");
		else if (GameUtils.equals(locationKey, path.getProperty("location2Key")))
			otherLocationKey = path.getProperty("location1Key");
		else
			throw new RuntimeException("Player is not at either end of the path they are deleting.");
		
		CachedEntity otherLocation = db.getEntity((Key)otherLocationKey);
		Object ownerKey = otherLocation.getProperty("ownerKey");
		
		/*if (ownerKey != null)
		{
			if(!GameUtils.equals(ownerKey, character.getProperty("userKey")))
				throw new UserErrorMessage("You cannot delete a house you do not own.");
		}*/
		
		if (ownerKey == null) // other end is not a house -- player must then be currently inside the house
			throw new UserErrorMessage("You must be outside of the house before you can delete it.");
		
		ds.delete(db.getDiscoveryByEntity(character.getKey(), path.getKey()));
		
		setJavascriptResponse(JavascriptResponse.FullPageRefresh);
	}
}
