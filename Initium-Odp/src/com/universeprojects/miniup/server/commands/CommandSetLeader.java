package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;


/**
 * Allows the current leader of a party to set a different party member as leader
 * 
 * Usage notes:
 * Checks if caller is party leader, and if charId is current member of th party.
 * 
 * Parameters:
 * 		charId - characterID to be set as the new leader 
 * 
 * @author NJ
 *
 */
public class CommandSetLeader extends Command {

	public CommandSetLeader(HttpServletRequest request, HttpServletResponse response) 
	{
		super(request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage 
	{
		ODPDBAccess db = getDB();

		// Verify parameter sanity
		Long charId;
		try {
			charId = Long.parseLong(parameters.get("charId"));
		} catch (Exception e) {
			throw new RuntimeException("SetLeader invalid call format, 'charId' is not a valid id.");
		}
		CachedEntity member = db.getCharacterById(charId);
		if (member==null)
			throw new RuntimeException("SetLeader invalid call format, 'charId' is not a valid id.");

		// Verify that the caller is the current party leader
		CachedEntity character = db.getCurrentCharacter(request);
		String partyCode = (String)character.getProperty("partyCode");
		if (partyCode==null || partyCode.trim().equals(""))
			throw new UserErrorMessage("You are not currently in a party and thus cannot do this.");
		// partyLeader is stored as an enum of "TRUE","FALSE"
		if ("TRUE".equals(character.getProperty("partyLeader"))==false)
			throw new UserErrorMessage("You are not currently the party leader and thus cannot do this.");
		
		// Check that caller status is set to Normal (or blank)
		String mode = (String)character.getProperty("mode");
		if (mode!=null && mode.equals("NORMAL")==false)
			throw new UserErrorMessage("You're too busy to swich leaders at the moment.");
		
		// Quick sanity check that charId is not the current leader
		if (charId.equals(character.getId()))
			throw new UserErrorMessage("You are already the leader of this party.");
		
		// Verify that charId is a member of the party
		if (partyCode.equals(member.getProperty("partyCode"))==false)
			throw new UserErrorMessage("This character is not a member of your party and thus you cannot do this.");
		
		// Assign new leader status.
		try {
			character.setProperty("partyLeader", "FALSE");
			member.setProperty("partyLeader", "TRUE");
			CachedDatastoreService ds = getDS();
			ds.put(character);
			ds.put(member);
		}
		catch(Exception e){
			throw new UserErrorMessage("Error while switching leader: "+e.getMessage());
		}
		setJavascriptResponse(JavascriptResponse.FullPageRefresh);
		
	}

}
