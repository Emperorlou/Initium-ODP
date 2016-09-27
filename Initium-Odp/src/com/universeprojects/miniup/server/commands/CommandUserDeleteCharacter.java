package com.universeprojects.miniup.server.commands;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.HtmlComponents;
import com.universeprojects.miniup.server.NotificationType;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.ODPDBAccess.CharacterMode;
import com.universeprojects.miniup.server.TradeObject;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class CommandUserDeleteCharacter extends Command {
	
	public CommandUserDeleteCharacter(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}
	
	public void run(Map<String,String> parameters) throws UserErrorMessage {
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		
		Key currentUser = db.getCurrentUserKey();
		Long characterId = tryParseId(parameters, "characterId");
		CachedEntity character = db.getEntity("Character",characterId);
		
		if(GameUtils.equals(character.getProperty("mode"), "COMBAT"))
			throw new UserErrorMessage("You can not delete a character that's in combat.");
		
		if(GameUtils.equals(character, db.getCurrentCharacter()))
			throw new UserErrorMessage("You can not delete the character you are currently using. Please switch characters and try again.");
		
		if(GameUtils.equals(currentUser, character.getProperty("userKey"))){
			character.setProperty("hitpoints", 0L);
			character.setProperty("mode", CharacterMode.DEAD.toString());
			character.setProperty("userKey", null);
			ds.put(character);
		}
	}
}