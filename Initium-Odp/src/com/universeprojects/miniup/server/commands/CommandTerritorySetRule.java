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
 * Checks if caller is Admin of owning group, and sets rule.
 * Purges all new trespassers.
 * 
 * Parameters:
 * 		rule - valid values: None/Whitelisted/OwningGroupOnly 
 * 
 * @author NJ
 *
 */
public class CommandTerritorySetRule extends Command {

	public CommandTerritorySetRule(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) 
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
		
		// Verify permission to set rule
		Key groupKey = (Key)character.getProperty("groupKey");
		if (groupKey==null)
			throw new UserErrorMessage("Only groups can control territories.");
		if (GameUtils.equals(groupKey, territory.getProperty("owningGroupKey"))==false)
			throw new UserErrorMessage("Your group doesn't control this territory.");
		if ("Admin".equals(character.getProperty("groupStatus"))==false)
			throw new UserErrorMessage("Only the admins of the group can set new territory rules.");
		
		// Verify rule parameter sanity
		TerritoryTravelRule travelRule = null;
		try {
			travelRule = TerritoryTravelRule.valueOf(parameters.get("rule"));
		} catch (Exception e) {
			throw new RuntimeException("TerritorySetRule invalid call format, 'rule' is not a valid rule.");
		}
		
		territory.setProperty("travelRule", travelRule.toString());
		
		// Purge all newly created trespassers
		new TerritoryService(db, territory).territoryPurgeCharacters(null, TerritoryCharacterFilter.Trespassing);
		
		// Commit to DB only after purge
		db.getDB().put(territory);
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup); 
	}

}
