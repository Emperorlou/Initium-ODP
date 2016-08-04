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
import com.universeprojects.miniup.server.services.CombatService;
import com.universeprojects.miniup.server.services.TerritoryService;
import com.universeprojects.miniup.server.services.TerritoryService.TerritoryCharacterFilter;
import com.universeprojects.miniup.server.services.TerritoryService.TerritoryTravelRule;


/**
 * Sets the characters that defend 
 * 
 * Usage notes:
 * Two possible usage versions:
 *   - as allowed char, set yourself as defending or not on the line you want
 *   - *DISABLED* as Admin, change the defensive line of characters that are already defending
 * 
 * Parameters:
 * 		*DISABLED* charId - the charId to set the defense position for (only for admins, null otherwise)
 * 		line - [0,1,2,3] the line to defend on (0 means not defending)
 * 
 * @author NJ
 *
 */
public class CommandTerritorySetDefense extends Command {

	public CommandTerritorySetDefense(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) 
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
		
		// Verify line param sanity
		String newStatus = parameters.get("line");
		if (GameUtils.isContainedInList("0,1,2,3", newStatus)==false)
			throw new RuntimeException("TerritorySetDefense invalid call format, 'line' is not a valid number.");
		
/* Admin version disabled for now
		// If charId was passed, use the admin version
		if (parameters.get("charId")!=null)
		{
			// Verify permission to set defense
			Key groupKey = (Key)character.getProperty("groupKey");
			if (groupKey==null)
				throw new UserErrorMessage("Only groups can control territories.");
			if (GameUtils.equals(groupKey, territory.getProperty("owningGroupKey"))==false)
				throw new UserErrorMessage("Your group doesn't control this territory.");
			if ("Admin".equals(character.getProperty("groupStatus"))==false)
				throw new UserErrorMessage("Only the admins of the group can change defense ranks.");
			
			// Verify defender validity
			character = db.getCharacterById(tryParseId(parameters, "charId"));
			if (character==null)
				throw new RuntimeException("TerritorySetDefense invalid call format, 'charId' is not a valid id.");
			String status = (String)character.getProperty("status");
			if (status==null || status.startsWith("Defending")==false)
				throw new UserErrorMessage("This character is currently not defending.");
			location = db.getEntity((Key)character.getProperty("locationKey"));
			if (GameUtils.equals(territory.getKey(), location.getProperty("territoryKey")))
				throw new UserErrorMessage("This character is currently not in the territory.");
			
			// Verify character is alive and not doing something else
			if (GameUtils.isPlayerIncapacitated(character))
				throw new UserErrorMessage("Your necromancy skills are lacking. The dead do not obey you.");
			String mode = (String)character.getProperty("mode");
			if (mode!=null && mode.equals("NORMAL")==false)
				throw new UserErrorMessage("This character is too distracted to receive new orders at the moment.");
		}
		else
*/
		{
			// Verify character is alive and not doing something else
			if (GameUtils.isPlayerIncapacitated(character))
				throw new UserErrorMessage("You are incapacitated and thus cannot do this.");
			String mode = (String)character.getProperty("mode");
			if (mode!=null && mode.equals("NORMAL")==false)
				throw new UserErrorMessage("You're too busy to switch ranks at the moment.");
			
			// Make sure character is allowed in territory
			if (new TerritoryService(db, territory).isAllowedIn(character)==false)
				throw new UserErrorMessage("You're not allowed to defend this territory.");
		}
		
		// Build status string 
		if (newStatus=="0")
			newStatus = "Normal";
		else
			newStatus = "Defending"+newStatus;
		character.setProperty("status", newStatus);
		
		db.getDB().put(character);
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
	}

}
