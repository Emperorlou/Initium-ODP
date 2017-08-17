package com.universeprojects.miniup.server.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.MainPageUpdateService;

/**
 * 
 * @author dylan
 * Allows a player to leave a party, if they are apart of one, and are if they are not the party leader while there are >= 3 party members.
 */
public class CommandPartyLeave extends Command {
	
	public CommandPartyLeave(ODPDBAccess db, HttpServletRequest request,
			HttpServletResponse response) {
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();

		CachedEntity character = db.getCurrentCharacter();
		if(character == null)
			throw new RuntimeException("Character is null in command");
		
		String partyCode = (String) character.getProperty("partyCode");
		if (GameUtils.isCharacterInParty(character)==false) 
			throw new UserErrorMessage("You are not currently in a party!");
		
		List<CachedEntity> party = db.getParty(ds, character);
		// If party.size() == 1, then null is returned, so we can assume there are
		// other members in the party.
		if(party != null)
			db.doRequestLeaveParty(ds, character);
		
		CachedEntity location = ds.getIfExists((Key)character.getProperty("locationKey"));
		MainPageUpdateService mpus = new MainPageUpdateService(db, db.getCurrentUser(), character, location, this);
		mpus.updatePartyView();
		mpus.updateButtonBar(); // Also sets the allow party join bit, so update button bar.
		
		// Use already retrieved keys here. Leaving party might have cleared it completely,
		// but we still need to update all the old members.
		List<Key> partyKeys = new ArrayList<Key>();
		for(CachedEntity member:party)
			if(member != null && GameUtils.equals(character.getKey(), member.getKey())==false)
				partyKeys.add(member.getKey());
		
		if(partyKeys.isEmpty()==false)
			db.sendMainPageUpdateForCharacters(ds, partyKeys, "updatePartyView");
	}
}
