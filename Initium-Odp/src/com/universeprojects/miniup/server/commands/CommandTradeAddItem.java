package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.HtmlComponents;
import com.universeprojects.miniup.server.NotificationType;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.TradeObject;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class CommandTradeAddItem extends Command {
	
	public CommandTradeAddItem(HttpServletRequest request, HttpServletResponse response)
	{
		super(request, response);
	}
	
	public void run(Map<String,String> parameters) throws UserErrorMessage {
		
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
			
		Long characterId = tryParseId(parameters,"characterId");
        CachedEntity otherCharacter = db.getEntity("Character", characterId);
        Long itemId = tryParseId(parameters,"itemId");
        CachedEntity item = db.getEntity("Item", itemId);
        TradeObject tradeObject = TradeObject.getTradeObjectFor(ds, db.getCurrentCharacter(request));
        
        if (item==null)
			throw new UserErrorMessage("Item doesn't exist.");
		
        db.addTradeItem(ds, db.getCurrentCharacter(request), item);
        db.sendNotification(ds, otherCharacter.getKey(), NotificationType.tradeChanged);
        
        Integer tradeVersion = tradeObject.getVersion();
        
        addCallbackData("tradeVersion",tradeVersion);
        addCallbackData("createTradeItemHtml",HtmlComponents.generatePlayerTradeItemHtml(item,otherCharacter));
	}
}