package com.universeprojects.miniup.server.commands;

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
 * Sets the territory navigation rule
 * 
 * Usage notes:
 * Checks if caller is Admin of owning group, and unsets ownership.
 * Afterwards everyone is purged from territory.
 * 
 * Parameters:
 * 
 * @author NJ
 *
 */
public class CommandTerritoryVacate extends Command {

	public CommandTerritoryVacate(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) 
	{
		super(db, request, response);
	}
	
	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage 
	{
		ODPDBAccess db = getDB();
		
		CachedEntity character = db.getCurrentCharacter(request);
		CachedEntity location = db.getEntity((Key)character.getProperty("locationKey"));
		CachedEntity territory = db.getEntity((Key)location.getProperty("territoryKey"));
		if (territory==null)
			throw new UserErrorMessage("You're not in a territory at the moment.");
		
		TerritoryService ts = new TerritoryService(db, territory);
		
		// Only admins are allowed to vacate
		if (ts.isTerritoryAdmin(character)==false)
			throw new UserErrorMessage("Only the admins of the owning group can vacate a territory.");
		
		territory.setProperty("owningGroupKey", null);
		
		//purge everyone
		ts.territoryPurgeCharacters(null, TerritoryCharacterFilter.All);
		
		// Commit to DB only after purge
		db.getDB().put(territory);
		// Since your char will have been relocated by the purge, no need to send another refresh here.
		
	}

}
