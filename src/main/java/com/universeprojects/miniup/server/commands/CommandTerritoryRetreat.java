package com.universeprojects.miniup.server.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.commands.framework.Command.JavascriptResponse;
import com.universeprojects.miniup.server.services.TerritoryService;
import com.universeprojects.miniup.server.services.TerritoryService.TerritoryCharacterFilter;
import com.universeprojects.miniup.server.services.TerritoryService.TerritoryTravelRule;


/**
 * Retreats a trespassing character from the territory if there are active defenders.
 * 
 * Usage notes:
 * Calls TerritoryService.canRetreat for easy call access for link generation.
 * 
 * Parameters:
 * 
 * @author NJ
 *
 */
public class CommandTerritoryRetreat extends Command {

	public CommandTerritoryRetreat(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) 
	{
		super(db, request, response);
	}
	
	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage 
	{
		ODPDBAccess db = getDB();
		
		CachedEntity character = db.getCurrentCharacter();
		CachedEntity location = db.getEntity((Key)character.getProperty("locationKey"));
		CachedEntity territory = db.getEntity((Key)location.getProperty("territoryKey"));
		if (territory==null)
			throw new UserErrorMessage("You're not in a territory at the moment.");
		
		TerritoryService ts = new TerritoryService(db, territory);
		
		String errorMsg = ts.getRetreatError(character, location);
		if (errorMsg!=null)
			throw new UserErrorMessage(errorMsg);
		
		//purge the character
		List<CachedEntity> characters = new ArrayList<CachedEntity>(1);
		characters.add(character);
		ts.territoryPurgeCharacters(characters, TerritoryCharacterFilter.All);
		
		// Since your char will have been relocated by the purge, no need to send another refresh here.
	}

}
