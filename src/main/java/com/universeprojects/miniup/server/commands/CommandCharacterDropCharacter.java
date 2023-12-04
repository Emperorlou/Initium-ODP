package com.universeprojects.miniup.server.commands;

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

/**
 * Drops the specified character to the current location. Revives if in a rest site.
 * 
 * @author SPFiredrake
 * 
 */
public class CommandCharacterDropCharacter extends Command {

	public CommandCharacterDropCharacter(ODPDBAccess db,
			HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		ODPDBAccess db = getDB();
		CachedEntity character = db.getCurrentCharacter();
		
		if(CommonChecks.checkCharacterIsZombie(character))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");
		
		CachedEntity location = db.getEntity((Key)character.getProperty("locationKey"));
		
		if(CommonChecks.checkCharacterIsBusy(character))
			throw new UserErrorMessage("You are currently busy and cannot drop this character");
		
		Long characterId = tryParseId(parameters, "characterId");
		CachedEntity dropCharacter = db.getEntity("Character", characterId);
		
		db.doCharacterDropCharacter(location, character, dropCharacter);
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
		
		if(GameUtils.isPlayerIncapacitated(dropCharacter) == false)
			db.sendMainPageUpdateForCharacter(db.getDB(), dropCharacter.getKey(), "updateFullPage_shortcut");
	}
}
