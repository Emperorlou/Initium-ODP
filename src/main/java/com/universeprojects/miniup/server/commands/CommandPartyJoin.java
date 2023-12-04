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
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.MainPageUpdateService;

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
		
		if(CommonChecks.checkCharacterIsZombie(character))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");
		
		CachedEntity partyCharacter = null;
		
		String mode = (String) character.getProperty("mode");
		if (mode != null && mode.equals("COMBAT"))
			throw new UserErrorMessage("You cannot join a party while in combat!");
		
		if(parameters.get("inputType").equals("characterId"))
		{
			partyCharacter = db.getEntity("Character", this.tryParseId(parameters, "characterId"));
		}
		if(parameters.get("inputType").equals("partyCode")) 
		{
			String toJoin = parameters.get("partyCode");
			if(toJoin == null || "".equals(toJoin))
				throw new RuntimeException("Specified party code is null!");
			
			partyCharacter = db.getPartyLeader(ds, toJoin, null);
		} 
		else if (parameters.get("inputType").equals("characterName")) 
		{
			String characterName = parameters.get("characterName");
			partyCharacter = db.getCharacterByName(characterName);
		}
		
		if(partyCharacter == null)
			throw new UserErrorMessage("Specified character is not in a group!");
		
		// Can only party with other HCM status characters.
		if(CommonChecks.checkIsHardcore(character) != CommonChecks.checkIsHardcore(partyCharacter))
		{
			String hardcoreMode = CommonChecks.checkIsHardcore(character) ? "hardcore" : "non-hardcore";
			throw new UserErrorMessage("You can only party with other " + hardcoreMode + " mode players.");
		}
		
		ds.beginBulkWriteMode();
		
		// This method contains all the validations necessary.
		db.doRequestJoinParty(ds, character, partyCharacter);
		
		
		MainPageUpdateService mpus = MainPageUpdateService.getInstance(db, db.getCurrentUser(), character, null, this);
		mpus.updatePartyView();
		
		
		List<CachedEntity> party = db.getParty(ds, character);
		// Not sure how this happens. Just create a new list with the two chars
		if (party==null)
			party = Arrays.asList(character, partyCharacter);
		else
			party.add(character);
		db.setPartyCache(party);
		
		
		ds.commitBulkWrite();
		
		List<Key> partyKeys = new ArrayList<Key>();
		for(CachedEntity member:party)
			if(member != null && GameUtils.equals(character.getKey(), member.getKey())==false)
				partyKeys.add(member.getKey());
		
		if(partyKeys.isEmpty()==false)
			db.sendMainPageUpdateForCharacters(ds, partyKeys, "updatePartyView");
		
		
	}
}
