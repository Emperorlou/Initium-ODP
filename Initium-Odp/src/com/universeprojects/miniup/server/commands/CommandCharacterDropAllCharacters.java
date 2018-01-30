package com.universeprojects.miniup.server.commands;

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

/**
 * Drops all characters at the specified location. Revives if in a rest site.
 * 
 * @author SPFiredrake
 * 
 */
public class CommandCharacterDropAllCharacters extends Command {

	public CommandCharacterDropAllCharacters(ODPDBAccess db,
			HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		ODPDBAccess db = getDB();
		CachedEntity character = db.getCurrentCharacter();
		CachedEntity location = db.getEntity((Key)character.getProperty("locationKey"));
		
		if(CommonChecks.checkCharacterIsBusy(character))
			throw new UserErrorMessage("You are currently busy and cannot drop this character");
		
		List<java.security.Key> updateKeys = new ArrayList<Key>();
		List<CachedEntity> carriedChars = getFilteredList("Character", "locationKey", character.getKey());
		for(CachedEntity charToDrop:carriedChars)
		{
			db.doCharacterDropCharacter(location, character, charToDrop);
			if(GameUtils.isPlayerIncapacitated(charToDrop) == false)
				updateKeys.add(charToDrop.getKey());
		}
		
		if(updateKeys.isEmpty() == false)
			db.sendMainPageUpdateForCharacters(db.getDB(), updateKeys, "updateFullPage_shortcut");
	}
}
