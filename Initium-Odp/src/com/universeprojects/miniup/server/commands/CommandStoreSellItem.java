package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.HtmlComponents;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
/** 
 * 
 * Sell an Item!
 * 
 */

public class CommandStoreSellItem extends Command {
	
	public CommandStoreSellItem(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}
	
	@Override
public void run(Map<String,String> parameters) throws UserErrorMessage {
		
		Long amount = Long.parseLong(parameters.get("amount"));
		Long itemId = Long.parseLong(parameters.get("itemId"));
		
		if (amount<0)
			throw new UserErrorMessage("You cannot sell an item for less than 0 gold.");
		
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();
		
		Key itemKey = KeyFactory.createKey("Item", itemId);
		CachedEntity item = db.getEntity(itemKey);
		
		if (db.checkCharacterHasItemEquipped(character, itemKey))
			throw new UserErrorMessage("Unable to sell this item, you currently have it equipped.");
		
		if (item==null)
			throw new UserErrorMessage("Item doesn't exist.");
		
		if (item.getProperty("containerKey").equals(character.getKey())==false)
			throw new UserErrorMessage("You do not have this item. You cannot sell an item that is not in your inventory.");
		
		if (db.checkItemBeingSoldAlready(character.getKey(), itemKey))
			throw new UserErrorMessage("You are already selling that item. If you want to change the price, remove the existing entry first.");
		
		CachedEntity saleItem = db.newSaleItem(ds, character, item, amount);

		// This is a special case
		if (CommonChecks.checkItemIsPremiumToken(item))
		{
			setPopupMessage("This item has been listed on the global exchange for premium tokens.");
		}
		
		// This is a special case
		if (CommonChecks.checkItemIsChippedToken(item))
		{
			setPopupMessage("This item has been listed on the global exchange for premium tokens.");
		}
		
		addCallbackData("createSellItem", HtmlComponents.generateSellItemHtml(db,saleItem,request));
		}
}
