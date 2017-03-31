package com.universeprojects.miniup.server.commands;

import java.util.Map;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.WebUtils;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.commands.framework.Command.JavascriptResponse;

/**
 * Checks to see if a character is stuck in a location with no discoverable paths available. If the character
 * is stuck, their locationKey is set to the parentLocationKey of their current location. 
 * 
 * @author papamarsh
 * 
 */
public class CommandCharacterAutofixStuckInLocation extends Command {

	public CommandCharacterAutofixStuckInLocation(ODPDBAccess db, HttpServletRequest request,
			HttpServletResponse response) {
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		ODPDBAccess db = ODPDBAccess.getInstance(request);
		CachedDatastoreService ds = getDS();
		
		//grab the current character and its location
		CachedEntity character = db.getCurrentCharacter();
		CachedEntity currentLocation = db.getEntity((Key) character.getProperty("locationKey"));
		if(currentLocation==null)
		{
			//If a character with null locationKey uses the command, a refresh will send them to their hometown
			setJavascriptResponse(JavascriptResponse.FullPageRefresh);
			return;
		}
		//grab the parent location of the character's current locationKey
		CachedEntity parentLocation = db.getParentLocation(ds, currentLocation);
		
		//special case for if user is in hell
		if(currentLocation==db.getLocationByName("Ninth Circle of Hell"))
		{
			throw new UserErrorMessage("Nice Try! But there's no escape from Hell :)");
		}
		
		//list the paths connected to the character's location
		List<CachedEntity> pathList = db.getPathsByLocation(currentLocation.getKey());
		for(CachedEntity checkPath:pathList)
		{
			CachedEntity checkPathLocation1 = db.getEntity((Key) checkPath.getProperty("location1Key"));
			CachedEntity checkPathLocation2 = db.getEntity((Key) checkPath.getProperty("location2Key"));
			Double checkPathDiscoveryChance = (Double) checkPath.getProperty("discoveryChance");
			
			//check if path is discoverable and both locationKeys are valid entities
			if(checkPathLocation1!=null && checkPathLocation2!=null && checkPathDiscoveryChance>0)
			{
				throw new UserErrorMessage("At least one exit path exists");
			}
		}
		//if no discoverable paths are found at the location, set character's locationKey to the parent
		character.setProperty("locationKey", parentLocation);
		//I think this should be a good enough fix, even if the parent is invalid since characters with
		//an invalid locationKey are sent to their hometownKey (or default spawn loc if hometown is invalid)
		setJavascriptResponse(JavascriptResponse.FullPageRefresh);
	}
}
