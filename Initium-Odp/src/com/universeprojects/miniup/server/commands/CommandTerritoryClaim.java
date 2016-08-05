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
import com.universeprojects.miniup.server.commands.framework.Command.JavascriptResponse;
import com.universeprojects.miniup.server.services.CombatService;
import com.universeprojects.miniup.server.services.TerritoryService;
import com.universeprojects.miniup.server.services.TerritoryService.TerritoryCharacterFilter;
import com.universeprojects.miniup.server.services.TerritoryService.TerritoryTravelRule;


/**
 * Sets the territory navigation rule
 * 
 * Usage notes:
 * Checks for defenders and if found enters combat.
 * If not, check if caller is admin of his group and if so, claim.
 * After claiming sets all group members to Defend and purges all others.
 * 
 * Parameters:
 * 
 * @author NJ
 *
 */
public class CommandTerritoryClaim extends Command {

	public CommandTerritoryClaim(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) 
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
		
		// Verify character is alive and not doing something else
		if (GameUtils.isPlayerIncapacitated(character))
			throw new UserErrorMessage("You are incapacitated and thus cannot do this.");
		String mode = (String)character.getProperty("mode");
		if (mode!=null && mode.equals("NORMAL")==false)
			throw new UserErrorMessage("You're too busy to try and claim a territory at the moment.");
		
		// Check if group already owns this territory to prevent attacking its own defenders
		Key groupKey = (Key)character.getProperty("groupKey");
		if (groupKey!=null && GameUtils.equals(groupKey, territory.getProperty("owningGroupKey")) &&
				("Member".equals(character.getProperty("groupStatus"))==true || "Admin".equals(character.getProperty("groupStatus"))==true))
			throw new UserErrorMessage("Your group already controls this territory.");
		
		// Check for defenders
		TerritoryService ts = new TerritoryService(db, territory);
		CachedEntity defender = ts.getTerritorySingleCharacter(TerritoryCharacterFilter.Defending, location);
		if (defender!=null)
		{
			// Attack defender
			new CombatService(db).enterCombat(character, defender, true);
			setJavascriptResponse(JavascriptResponse.FullPageRefresh); 
			return;
		}
		
		// Verify permission to claim
		if (groupKey==null)
			throw new UserErrorMessage("Only groups can control territories.");
		if ("Admin".equals(character.getProperty("groupStatus"))==false)
			throw new UserErrorMessage("Only the admins of the group can claim a territory.");
		
		territory.setProperty("owningGroupKey", groupKey);
		// reset fields to default 
		territory.setProperty("characterWhitelist", null);
		territory.setProperty("groupWhitelist", null);
		territory.setProperty("travelRule", TerritoryTravelRule.OwningGroupOnly.toString());
		
		// Commit to DB early to prevent another group claiming during purge
		CachedDatastoreService ds = db.getDB();
		ds.put(territory);
		
		// Cache all characters to prevent having to get them twice
		List<CachedEntity> characters = ts.getTerritoryAllCharactersUnsorted(TerritoryCharacterFilter.All);
		
		// Default all group members to Defending rank 2, default caller to Defending rank 3
		for (CachedEntity c : characters)
		{
			if (GameUtils.equals(groupKey, c.getProperty("groupKey")) &&
				("Member".equals(c.getProperty("groupStatus"))==true || "Admin".equals(c.getProperty("groupStatus"))==true))
			{
				if (GameUtils.equals(c.getKey(), character.getKey()))
					c.setProperty("status", "Defending3");
				else
					c.setProperty("status", "Defending2");
				ds.put(c);
			}
		}
		
		//purge all but group
		ts.territoryPurgeCharacters(characters, TerritoryCharacterFilter.Trespassing);
		
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup); 
		
	}

}
