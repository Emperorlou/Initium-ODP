package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.HtmlComponents;
import com.universeprojects.miniup.server.NotificationType;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.TradeObject;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class CommandTradeAddItem extends Command {
	
	public CommandTradeAddItem(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}
	
	public void run(Map<String,String> parameters) throws UserErrorMessage {
		
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
			
		CachedEntity character = db.getCurrentCharacter(request);
		Key otherCharacter = (Key) character.getProperty("combatant");
        Long itemId = tryParseId(parameters,"itemId");
        CachedEntity item = db.getEntity("Item", itemId);
        TradeObject tradeObject = TradeObject.getTradeObjectFor(ds, character);
        
        if (item==null)
			throw new UserErrorMessage("Item doesn't exist.");
		
        db.addTradeItem(ds, character, item);
        db.sendNotification(ds, otherCharacter, NotificationType.tradeChanged);
        
        Integer tradeVersion = tradeObject.getVersion();
        
        addCallbackData("tradeVersion",tradeVersion);
        addCallbackData("createTradeItem",HtmlComponents.generatePlayerTradeItemHtml(item));
	}
}