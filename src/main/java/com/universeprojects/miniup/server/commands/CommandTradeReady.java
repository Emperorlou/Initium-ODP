package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.NotificationType;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.TradeObject;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.MainPageUpdateService;

public class CommandTradeReady extends Command {
	
	public CommandTradeReady(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}
	
	public void run(Map<String,String> parameters) throws UserErrorMessage {
		
		
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		Integer version = Integer.parseInt(parameters.get("tradeVersion"));
		CachedEntity character = db.getCurrentCharacter();
		Key otherCharacter = (Key) character.getProperty("combatant");

		TradeObject tradeObject = TradeObject.getTradeObjectFor(ds, character);
		if (tradeObject==null || tradeObject.isCancelled())
			throw new UserErrorMessage("Trade has been cancelled.");
		if (tradeObject.isComplete())
			throw new UserErrorMessage("Trade is already complete.");
		
		db.setTradeReady(ds, tradeObject, db.getCurrentCharacter(), version);
		
    	db.sendNotification(ds, otherCharacter, NotificationType.tradeChanged);
    	if (tradeObject.isComplete() == true)
    	{
    		String complete = "complete";
    		addCallbackData("tradeComplete",complete);
    		
    		MainPageUpdateService service = MainPageUpdateService.getInstance(db, db.getCurrentUser(), character, null, this);
    		service.updateMoney();
    		
    		// Also update the money of the guy on the other side
    		db.sendMainPageUpdateForCharacter(ds, otherCharacter, "updateMoney");
    	}
    	else setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
    	
	}
}