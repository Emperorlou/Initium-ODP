package com.universeprojects.miniup.server.commands;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.HtmlComponents;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class CommandStoreBuyItem extends Command {
	
	public CommandStoreBuyItem(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
		}
	
	public void run(Map<String,String> parameters) throws UserErrorMessage {
		
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		
		Long saleItemId = Long.parseLong(parameters.get("saleItemId"));
		Long characterId = Long.parseLong(parameters.get("characterId"));
		
		
		CachedEntity character = db.getCurrentCharacter(request);
		CachedEntity saleItem = db.getEntity("SaleItem", saleItemId);
		CachedEntity item = db.getEntity((Key)saleItem.getProperty("itemKey"));
		CachedEntity storeCharacter = db.getEntity("Character", characterId);
		
		addCallbackData("createStoreItem", HtmlComponents.generateStoreItemHtml(db,storeCharacter,item,saleItem,request));
		
		if (saleItem==null)
			throw new UserErrorMessage("This item has been taken down. The owner is no longer selling it.");
		if ("Sold".equals(saleItem.getProperty("status")))
			throw new UserErrorMessage("The owner of the store has already sold this item.");
		if ("Hidden".equals(saleItem.getProperty("status")))
			throw new UserErrorMessage("The owner of the store is not selling this item at the moment.");

		Long cost = (Long)saleItem.getProperty("dogecoins");
		if (cost==null)
			throw new UserErrorMessage("The sale item is not setup properly. It has no cost.");
		
		
		CachedEntity sellingCharacter = db.getEntity((Key)saleItem.getProperty("characterKey"));
		if (ODPDBAccess.CHARACTER_MODE_MERCHANT.equals(sellingCharacter.getProperty("mode"))==false)
			throw new UserErrorMessage("The owner of the store is not selling at the moment.");
		if (((Key)sellingCharacter.getProperty("locationKey")).getId()!=((Key)character.getProperty("locationKey")).getId())
			throw new UserErrorMessage("You are not in the same location as the seller. You can only buy from a merchant who is in the same location as you.");

		if (character.getKey().getId() == sellingCharacter.getKey().getId())
			throw new UserErrorMessage("You cannot buy items from yourself.");
		
		Double storeSale = (Double)sellingCharacter.getProperty("storeSale");
		if (storeSale==null) storeSale = 100d;

		cost=Math.round(cost.doubleValue()*(storeSale/100));
		
		if (cost>(Long)character.getProperty("dogecoins"))
			throw new UserErrorMessage("You do not have enough funds to buy this item. You have "+character.getProperty("dogecoins")+" and it costs "+saleItem.getProperty("dogecoins")+".");	
		if (item==null)
			throw new UserErrorMessage("The item being sold has been removed.");
		if (((Key)item.getProperty("containerKey")).getId()!=sellingCharacter.getKey().getId())
			throw new UserErrorMessage("The item you tried to buy is not actually in the seller's posession. Purchase has been cancelled.");
		
		
		if (cost<0)
			throw new UserErrorMessage("You cannot buy a negatively priced item.");
		
		ds.beginTransaction();
		try
		{
			saleItem.setProperty("status", "Sold");
			saleItem.setProperty("soldTo", character.getKey());
			sellingCharacter.setProperty("dogecoins", ((Long)sellingCharacter.getProperty("dogecoins"))+cost);
			character.setProperty("dogecoins", ((Long)character.getProperty("dogecoins"))-cost);
			item.setProperty("containerKey", character.getKey());
			item.setProperty("movedTimestamp", new Date());
			
			ds.put(saleItem);
			ds.put(sellingCharacter);
			ds.put(character);
			ds.put(item);
			
			ds.commit();
		}
		finally
		{
			ds.rollbackIfActive();
		}
	}
}