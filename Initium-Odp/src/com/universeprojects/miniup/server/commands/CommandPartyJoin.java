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
 * 
 * 
 * @author dylan
 * 
 * Allows a player to join a party if they are already not apart of a party, not in combat, the party is accepting members, or already apart of the same party,
 *
 */
public class CommandPartyJoin extends Command {

	public CommandPartyJoin(ODPDBAccess db, HttpServletRequest request,
			HttpServletResponse response) {
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();

		CachedEntity character = db.getCurrentCharacter();
		String toJoin = "";
		
		if(parameters.get("inputType").equals("partyCode")) {
			toJoin = parameters.get("partyCode");
		} else if (parameters.get("inputType").equals("characterName")) {
			String characterName = parameters.get("characterName");
			CachedEntity otherCharacter = db.getCharacterByName(characterName);
			if(otherCharacter == null) {
				throw new UserErrorMessage("Character with name of "+characterName+" does not exist!");
			}
			toJoin = (String) otherCharacter.getProperty("partyCode");
		}
		
		String currentParty = (String) character.getProperty("partyCode");
		
		if (toJoin.equals(currentParty)) {
			throw new UserErrorMessage("You are already apart of this party!");
		}
		if (currentParty != null && !currentParty.trim().equals("")) {
			throw new UserErrorMessage("You are already in a party! You must leave this one first!");
		}
		
		CachedEntity leader = db.getPartyLeader(ds, toJoin, null);
		if (leader.getProperty("partyJoinsAllowed").equals("FALSE")) {
			throw new UserErrorMessage("This party is not accepting members currently!");
		}
		String mode = (String) character.getProperty("mode");
		if (mode != null && mode.equals("COMBAT")) {
			throw new UserErrorMessage("You cannot join a party while in combat!");
		}
		
		character.setProperty("partyCode", toJoin);
		character.setProperty("partyLeader", "FALSE");
		
		ds.put(character);
	}
}
