package com.universeprojects.miniup.server.commands;

import java.util.List;
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

public class CommandTradeRemoveItem extends Command {
	
	public CommandTradeRemoveItem(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}
	
	public void run(Map<String,String> parameters) throws UserErrorMessage {
		
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		Long itemId = tryParseId(parameters,"itemId");
		CachedEntity character = db.getCurrentCharacter();
		Key otherCharacter = (Key) character.getProperty("combatant");
        CachedEntity item = db.getEntity("Item", itemId);
        TradeObject tradeObject = TradeObject.getTradeObjectFor(ds, character);
        List<CachedEntity> saleItems = db.getSaleItemsFor(character.getKey());
        
        if (tradeObject==null || tradeObject.isCancelled())
        {
            addCallbackData("tradeCancelled", true);
            throw new UserErrorMessage("Trade has been cancelled.");
        }
        		
        if (item==null)
            throw new UserErrorMessage("Item does not exist.");
        
        	tradeObject.removeObject(ds, db.getCurrentCharacter(), item);
            db.sendNotification(ds, otherCharacter, NotificationType.tradeChanged);
        
        Integer tradeVersion = tradeObject.getVersion();
        
        addCallbackData("tradeVersion",tradeVersion);
        addCallbackData("createTradeInvItem",HtmlComponents.generateTradeInvItemHtml(item, saleItems, db, ds, request));
        return;
	}
}