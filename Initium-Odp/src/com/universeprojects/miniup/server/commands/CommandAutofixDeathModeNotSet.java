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
 * Checks to see if a character has non-positive HP but did not correctly have its mode changed to
 * "Dead" or "Unconscious". If so, set mode to unconscious and clear equipment & combatant. If character
 * is in a RestSite location, then trigger their revival. 
 * 
 * @author papamarsh
 * 
 */
public class CommandAutofixDeathModeNotSet extends Command {

	public CommandAutofixDeathModeNotSet(ODPDBAccess db, HttpServletRequest request,
			HttpServletResponse response) {
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		ODPDBAccess db = ODPDBAccess.getInstance(request);
		CachedDatastoreService ds = getDS();
		
		//grab the current character and its location
		CachedEntity character = db.getCurrentCharacter();
		
		if(CommonChecks.checkCharacterIsZombie(character))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");
		
		CachedEntity currentLocation = db.getEntity((Key) character.getProperty("locationKey"));
		
		//grab the character's current hitpoints and mode
		Double hitpoints = (Double) character.getProperty("hitpoints");
		String mode = (String) character.getProperty("mode");
		
		//check for non-positive HP while mode is not either unconscious or dead
		//we're not using checkCharacterIsUnconscious and checkCharacterIsDead here because the 
		//unconscious check would return true for characters affected by this bug. 
		if(hitpoints<=0 && "UNCONSCIOUS".equals(mode)==false && "DEAD".equals(mode)==false)
		{
			//clear combatant
			character.setProperty("combatant", null);
			
			//check if the current location is a rest or camp site and revive them if so
			if("RestSite".equals(currentLocation.getProperty("type")) || "CampSite".equals(currentLocation.getProperty("type")))
			{
				//revive character to 1 HP
				character.setProperty("hitpoints", 1d);
                
				//clear equipment slots 
				for (String slot:ODPDBAccess.EQUIPMENT_SLOTS)
					character.setProperty("equipment" + slot, null);

				//set mode to normal
                character.setProperty("mode", "NORMAL");
			}
			
			else character.setProperty("mode", "UNCONSCIOUS");
			
			//save character and refresh screen
			ds.put(character);
			setJavascriptResponse(JavascriptResponse.FullPageRefresh);
		}
		
		else if(hitpoints>0)
			throw new UserErrorMessage("Your hitpoints is greater than 0!");
		
		else if("UNCONSCIOUS".equals(mode))
			throw new UserErrorMessage("You're already unconscious! Get someone to save you, quick!");
		
		else if("DEAD".equals(mode))
			throw new UserErrorMessage("RIP. You're already dead!");
		
		else
			throw new UserErrorMessage("Something seems to have gone wrong, please let Papa know!");
	}
}
