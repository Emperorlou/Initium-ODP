package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.NotificationType;
import com.universeprojects.miniup.server.ODPDBAccess;
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
		
		Long characterId = tryParseId(parameters,"characterId");
        CachedEntity otherCharacter = db.getEntity(KeyFactory.createKey("Character", characterId));
          
        db.startTrade(null, db.getCurrentCharacter(request), otherCharacter);
        db.sendNotification(ds, otherCharacter.getKey(), NotificationType.tradeStarted);
        addCallbackData("tradePrompt","You are now trading with "+otherCharacter.getProperty("name")+".");
        return;
	}
}