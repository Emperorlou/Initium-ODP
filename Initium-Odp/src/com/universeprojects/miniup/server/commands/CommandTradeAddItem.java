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

public class CommandTradeAddItem extends Command {
	
	public CommandTradeAddItem(HttpServletRequest request, HttpServletResponse response)
	{
		super(request, response);
	}
	
	public void run(Map<String,String> parameters) throws UserErrorMessage {
		
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
			
		Long characterId = Long.parseLong("characterId");
        CachedEntity otherCharacter = db.getEntity(KeyFactory.createKey("Character", characterId));
		Long itemId = Long.parseLong(parameters.get("itemId"));
        CachedEntity item = db.getEntity("Item", itemId);
        
        if (item==null)
			throw new UserErrorMessage("Item doesn't exist.");
		
        db.addTradeItem(ds, db.getCurrentCharacter(request), item);
        db.sendNotification(ds, otherCharacter.getKey(), NotificationType.tradeChanged);
        
        return;
	}
}