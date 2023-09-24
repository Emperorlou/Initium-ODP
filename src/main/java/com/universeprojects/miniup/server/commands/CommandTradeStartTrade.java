package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.NotificationType;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.TradeObject;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class CommandTradeStartTrade extends Command {
	
	public CommandTradeStartTrade(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}
	
	public void run(Map<String,String> parameters) throws UserErrorMessage {
		
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();
		
		if(CommonChecks.checkCharacterIsZombie(character))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");
		
		CachedEntity otherCharacter = null;
		
		if(parameters.get("inputType").equals("characterId")) {
			Long characterId = tryParseId(parameters,"characterId");
			otherCharacter = db.getEntity(KeyFactory.createKey("Character", characterId));
		} else if (parameters.get("inputType").equals("characterName")) {
			String characterName = parameters.get("characterName");
			otherCharacter = db.getCharacterByName(characterName);
			if(otherCharacter == null) {
				throw new UserErrorMessage("Character with name of "+characterName+" does not exist!");
			}
		}
        
        
        if (GameUtils.equals(((Key)character.getProperty("locationKey")),((Key)otherCharacter.getProperty("locationKey")))==false)
			throw new UserErrorMessage("You cannot start a trade with a character that is not in your location.");
		
		TradeObject.startNewTrade(ds, character, otherCharacter);
        db.sendNotification(ds, otherCharacter.getKey(), NotificationType.tradeStarted);
        addCallbackData("tradePrompt","You are now trading with "+otherCharacter.getProperty("name")+".");
        return;
	}
}