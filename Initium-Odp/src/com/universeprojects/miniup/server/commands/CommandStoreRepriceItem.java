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
import com.universeprojects.miniup.server.HtmlComponents;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.ContainerService;

/**
 * Reprice an existing sale item. Make sure the character is not vending,
 * as we don't want to run into situations where they reprice an item
 * when someone is about to buy it.
 * @author spotupchik
 *
 */
public class CommandStoreRepriceItem extends Command 
{

	public CommandStoreRepriceItem(ODPDBAccess db, HttpServletRequest request,
			HttpServletResponse response) 
	{
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage,
			UserRequestIncompleteException 
	{
		Long saleItemId = Long.parseLong(parameters.get("saleItemId"));
		Long amount = null;
		try
		{
			amount = Long.parseLong(parameters.get("amount").trim());
		}
		catch(Exception e)
		{
			throw new UserErrorMessage("Invalid number. Please specify a whole number (no decimal places).");
		}

		if (amount<0)
			throw new UserErrorMessage("You cannot sell an item for less than 0 gold.");
		
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();
		
		if(GameUtils.isPlayerIncapacitated(character))
			throw new UserErrorMessage("You are currently incapacitated, so probably shouldn't be worrying about what's in your shop.");
		
		if(CommonChecks.checkCharacterIsVending(character))
			throw new UserErrorMessage("You cannot reprice an item when your shop is open. Please stop vending first.");
		
		Key saleItemKey = KeyFactory.createKey("SaleItem", saleItemId);
		CachedEntity saleItem = db.getEntity(saleItemKey);
		
		if(GameUtils.equals(saleItem.getProperty("characterKey"), character.getKey())==false)
			throw new UserErrorMessage("You can only reprice items that you are selling.");
		
		if("Selling".equals(saleItem.getProperty("status"))==false)
			throw new UserErrorMessage("This item has already sold and cannot be repriced.");
		
		Key itemKey = (Key)saleItem.getProperty("itemKey");
		if(itemKey == null)
			throw new RuntimeException("Sale item does not reference an Item entity");
		
		CachedEntity item = db.getEntity(itemKey);
		if (item==null)
			throw new UserErrorMessage("Item no longer exists.");
		
		ContainerService container = new ContainerService(db);
		
		if (db.checkCharacterHasItemEquipped(character, itemKey))
			throw new UserErrorMessage("Unable to sell this item, you currently have it equipped.");
		
		if (container.contains(character, item)==false)
			throw new UserErrorMessage("You do not have this item. You cannot sell an item that is not in your inventory.");
		
		saleItem.setProperty("dogecoins", amount);
		ds.put(saleItem);
		
		addCallbackData("repriceItem", HtmlComponents.generateSellItemHtml(db,saleItem,request));
	}

}
