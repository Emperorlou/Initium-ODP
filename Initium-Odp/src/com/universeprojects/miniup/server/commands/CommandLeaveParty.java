package com.universeprojects.miniup.server.commands;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

/**
 * 
 * @author dylan
 * Allows a player to leave a party, if they are apart of one, and are if they are not the party leader while there are >= 3 party members.
 */
public class CommandLeaveParty extends Command {
	
	public CommandLeaveParty(ODPDBAccess db, HttpServletRequest request,
			HttpServletResponse response) {
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();

		CachedEntity character = db.getCurrentCharacter();
		String partyCode = (String) character.getProperty("partyCode");
		if (partyCode == null || partyCode.trim().equals("")) {
			throw new UserErrorMessage("You are not currently in a party!");
		}
		
		if (character.getProperty("mode").equals("COMBAT")) {
			throw new UserErrorMessage("You cannot leave a party while in combat!");
		}
		
		List<CachedEntity> party = db.getParty(ds, character);
		if (character.getProperty("partyLeader").equals("TRUE") && !(party == null || party.size() <= 2)) { 
			//Check if getParty is null to see if they were the last member in the party, 
			//if they were the last member in the party, then they can leave without promoting new leader. or if the party is comprised 
			//with just two people, a new party leader is also not needed.
			throw new UserErrorMessage("You are currently the party leader! In order to leave make someone else the party leader!");
		}
		
		db.doLeaveParty(ds, character);
	}
}
