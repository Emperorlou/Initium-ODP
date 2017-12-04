package com.universeprojects.miniup.server.commands;

import java.util.List;
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

public class CommandTradeAddAllItems extends Command {
	
	public CommandTradeAddAllItems(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}
	
	public void run(Map<String,String> parameters) throws UserErrorMessage {
		
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		
		CachedEntity character = db.getCurrentCharacter();
		List<CachedEntity> items = db.getFilteredList("Item", "containerKey", character.getKey());
		Key otherCharacter = (Key) character.getProperty("combatant");

		TradeObject tradeObject = TradeObject.getTradeObjectFor(ds, character);
		if (tradeObject==null || tradeObject.isCancelled())
		{
			addCallbackData("tradeCancelled", true);
			throw new UserErrorMessage("Trade has been cancelled.");
		}
		if (tradeObject.isComplete())
			throw new UserErrorMessage("Trade is already complete.");
		
		
		boolean tooManyItems = false;
		for(int i = items.size()-1;i>=0;i--)
		{
			CachedEntity item = items.get(i);
			
			if (db.checkCharacterHasItemEquipped(character, item.getKey())){
				items.remove(i);
				continue;
			}
            if (db.checkItemIsVending(character.getKey(), item.getKey())){
            	items.remove(i);
                continue;
            }
            if (items.size()>200)
            {
            	items.remove(i);
            	tooManyItems = true;
            	continue;
            }
		}
        
		if (tooManyItems)
			setPopupMessage("There were too many items so only the first 200 were added.");
		
		db.addTradeItems(ds, tradeObject, db.getCurrentCharacter(), items);
        db.sendNotification(ds, otherCharacter, NotificationType.tradeChanged);
        
        Integer tradeVersion = tradeObject.getVersion();
        addCallbackData("tradeVersion", tradeVersion);

        setJavascriptResponse(JavascriptResponse.ReloadPagePopup);

	}
}