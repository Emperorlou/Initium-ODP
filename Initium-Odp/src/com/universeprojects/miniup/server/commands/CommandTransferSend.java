package com.universeprojects.miniup.server.commands;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.MainPageUpdateService;

public class CommandTransferSend extends Command{

	public CommandTransferSend(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException {
		
		db.getDB().beginTransaction();
		
		CachedEntity currentUser = db.getCurrentUser();
		CachedEntity currentCharacter = db.getCurrentCharacter();
		
		if(CommonChecks.checkCharacterIsIncapacitated(currentCharacter))
			throw new UserErrorMessage("You cannot transfer an incapacitated character.");
		if(CommonChecks.checkCharacterIsZombie(currentCharacter))
			throw new UserErrorMessage("Braaaaaaaaaaaaaaaaaaaaaaaaains.....");
		
		String email = parameters.get("email");
		
		List<CachedEntity> results = db.getFilteredList("User", "email", email);
		
		if(results.size() == 0)
			throw new UserErrorMessage("Invalid email.");
		if(results.size() != 1)
			throw new UserErrorMessage("Unexpected error.");
		
		CachedEntity targetUser = results.get(0);
		
		if(false == GameUtils.equals(targetUser.getProperty("transferCharacterName"), currentCharacter.getProperty("name")))
			throw new UserErrorMessage("The target user is not ready to accept this character.");
		
		List<CachedEntity> userChars = db.getUserCharacters(currentUser);
		if(userChars.size() == 1)
			throw new UserErrorMessage("You only have one character and therefore cannot transfer it.");
		
		List<CachedEntity> targetChars = db.getUserCharacters(targetUser);
		if(targetChars.size() >= (Long)targetUser.getProperty("maximumCharacterCount"))
			throw new UserErrorMessage("The target user already has the maximum number of characters allowed.");
		
		//Give the current character to the target user, and then find a suitable replacement for the current user.
		currentCharacter.setProperty("userKey", targetUser.getKey());		
		for(CachedEntity ce : userChars) {
			if(false == GameUtils.equals(ce.getKey(), currentCharacter.getKey())) {
				currentUser.setProperty("characterKey", ce.getKey());
				break;
			}
		}
		
		targetUser.setProperty("transferCharacterName", null);
		
		db.getDB().put(currentCharacter, currentUser, targetUser);
		
		db.getDB().commit();
		
		//now delete all discoveries that point towards a player house. This is done outside of the transaction on purpose.
		//First, grab all the towns.
		List<CachedEntity> towns = db.getFilteredList("Location", "type", "Town");
		for(CachedEntity town : towns) {
			
			//Grab all of the character's discoveries in that town.
			List<CachedEntity> discoveries = db.getDiscoveriesForCharacterAndLocation(currentCharacter.getKey(), town.getKey(), true);
			
			for(CachedEntity discovery : discoveries) {
				//if that discovery is for an owned location(house), we delete it.
				CachedEntity location = db.getEntity((Key) discovery.getProperty("entityKey"));
				
				if(location.getProperty("ownerKey") != null)
					db.getDB().delete(discovery);
			}
		}
		
		MainPageUpdateService mpus = MainPageUpdateService.getInstance(db, currentUser, currentCharacter, db.getEntity((Key) currentCharacter.getProperty("locationKey")), this);
		mpus.updateFullPage_shortcut(true);
	}

}
