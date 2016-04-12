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
		// Get the DB access object. We use this for most DB communication. It is a place for reusable methods that access the DB.
		ODPDBAccess db = getDB();

		// Check if we're logged in or not. We shouldn't be able to see this since commands cannot be used unless the user is logged in.
		if (db.isLoggedIn(request)==false)
			throw new UserErrorMessage("You are not currently logged in and thus cannot do this.");
		
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
		if (partyCode==null || "".equals(partyCode))
			throw new UserErrorMessage("You are not currently in a party and thus cannot do this.");
		if (Boolean.FALSE.equals(character.getProperty("partyLeader")))
			throw new UserErrorMessage("You are not currently the party leader and thus cannot do this.");
		
		// Quick sanity check that charId is not the current leader
		if (charId==character.getId())
			throw new UserErrorMessage("You are already the leader of this party.");
		
		// Verify that charId is a member of the party
		if (partyCode.equals(member.getProperty("partyCode"))==false)
			throw new UserErrorMessage("This character is not a member of your party and thus you cannot do this.");
		
		// Assign new leader status.
		try {
			character.setProperty("partyLeader", false);
			member.setProperty("partyLeader", true);
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
