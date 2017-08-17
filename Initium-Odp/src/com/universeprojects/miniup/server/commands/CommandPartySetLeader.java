package com.universeprojects.miniup.server.commands;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.universeprojects.miniup.server.services.MainPageUpdateService;


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
public class CommandPartySetLeader extends Command {

	public CommandPartySetLeader(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) 
	{
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage 
	{
		ODPDBAccess db = getDB();

		// Verify parameter sanity
		CachedEntity member = db.getCharacterById(tryParseId(parameters, "charId"));
		if (member==null)
			throw new RuntimeException("SetLeader invalid call format, 'charId' is not a valid id.");

		// Verify that the caller is the current party leader
		CachedEntity character = db.getCurrentCharacter();
		String partyCode = (String)character.getProperty("partyCode");
		if (partyCode==null || partyCode.trim().equals(""))
			throw new UserErrorMessage("You are not currently in a party and thus cannot do this.");
		// partyLeader is stored as an enum of "TRUE","FALSE"
		if ("TRUE".equals(character.getProperty("partyLeader"))==false)
			throw new UserErrorMessage("You are not currently the party leader and thus cannot do this.");
		
		// Can't switch leader when incapacitated
		if (GameUtils.isPlayerIncapacitated(character))
			throw new UserErrorMessage("You are incapacitated and thus cannot do this.");
		
		// Make sure player isn't already doing something else
		String mode = (String)character.getProperty("mode");
		if (mode!=null && mode.equals("NORMAL")==false)
			throw new UserErrorMessage("You're too busy to switch leaders at the moment.");
		
		// Quick sanity check that charId is not the current leader
		if (GameUtils.equals(character.getKey(), member.getKey()))
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
			
			List<CachedEntity> party = db.getParty(null, character);
			if(party == null) party = Arrays.asList(character, member);
			CachedEntity location = ds.getIfExists((Key)character.getProperty("locationKey"));
			MainPageUpdateService mpus = new MainPageUpdateService(db, db.getCurrentUser(), character, location, this);
			mpus.updatePartyView();
			mpus.updateButtonBar(); // Also sets the allow party join bit, so update button bar.
			
			List<Key> partyKeys = new ArrayList<Key>();
			for(CachedEntity partyMember:party)
			{
				db.sendGameMessage(ds, character, member.getProperty("name") + " is now the party leader.");
				if(partyMember != null && GameUtils.equals(character.getKey(), partyMember.getKey())==false)
					partyKeys.add(partyMember.getKey());
			}
			
			if(partyKeys.isEmpty()==false)
				db.sendMainPageUpdateForCharacters(ds, partyKeys, "updatePartyView");
		}
		catch(Exception e){
			throw new UserErrorMessage("Error while switching leader: "+e.getMessage());
		}
	}

}
