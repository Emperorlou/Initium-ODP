package com.universeprojects.miniup.server.commands;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.JspSnippets;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.commands.framework.Command.JavascriptResponse;
/** 
 * 
 * Sell an Item!
 * 
 */

public class CommandStoreSellItem<character> extends Command {
	
	public CommandStoreSellItem(HttpServletRequest request, HttpServletResponse response)
	{
		super(request, response);
	}
	
	@Override
public void run(Map<String,String> parameters) throws UserErrorMessage {
		
		Long amount = (long) Integer.parseInt(parameters.get("amount"));
		Long itemId = Long.parseLong(parameters.get("itemId"));
		
		if (amount<0)
			throw new UserErrorMessage("You cannot sell an item for less than 0 gold.");
		
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter(request);
		CachedEntity item = db.getEntity("Item", tryParseId(parameters, "itemId"));
		
		Key itemKey = KeyFactory.createKey("Item", (long) itemId);
		
		if (db.checkCharacterHasItemEquipped(character, itemKey))
			throw new UserErrorMessage("Unable to sell this item, you currently have it equipped.");
		
		if (item==null)
			throw new UserErrorMessage("Item doesn't exist.");
		
		if (item.getProperty("containerKey").equals(character.getKey())==false)
			throw new UserErrorMessage("You do not have this item. You cannot sell an item that is not in your inventory.");
		
		if (db.checkItemBeingSoldAlready(character.getKey(), itemKey))
			throw new UserErrorMessage("You are already selling that item. If you want to change the price, remove the existing entry first.");
		
		db.newSaleItem(ds, character, item, amount);
	}
	
}