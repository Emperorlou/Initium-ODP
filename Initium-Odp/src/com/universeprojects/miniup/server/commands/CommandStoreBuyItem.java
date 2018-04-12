package com.universeprojects.miniup.server.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.HtmlComponents;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.MainPageUpdateService;

public class CommandStoreBuyItem extends Command {
	
	public CommandStoreBuyItem(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
		}
	
	public void run(Map<String,String> parameters) throws UserErrorMessage {
		
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		
		Long saleItemId = Long.parseLong(parameters.get("saleItemId"));
		Long characterId = Long.parseLong(parameters.get("characterId"));
		Long buyQuantity = 1L;
		try
		{
			if(parameters.containsKey("quantity")) buyQuantity = Long.parseLong(parameters.get("quantity"));
		}
		catch(Exception ex) 
		{}
		
		CachedEntity character = db.getCurrentCharacter();
		CachedEntity saleItem = db.getEntity("SaleItem", saleItemId);
		if (saleItem==null)
			throw new UserErrorMessage("This item has been taken down. The owner is no longer selling it.");
		CachedEntity item = db.getEntity((Key)saleItem.getProperty("itemKey"));
		CachedEntity storeCharacter = db.getEntity("Character", characterId);
		if (item==null)
			throw new UserErrorMessage("The item being sold has been removed.");
		if(buyQuantity < 1)
			throw new UserErrorMessage("You can only buy a positive quantity of items.");
		
		boolean isPremiumMembership = false;
		if (CommonChecks.checkItemIsPremiumToken(item) || CommonChecks.checkItemIsChippedToken(item))
			isPremiumMembership = true;
		
		if (isPremiumMembership && GameUtils.equals(character.getProperty("userKey"), storeCharacter.getProperty("userKey")))
			throw new UserErrorMessage("You cannot buy premium tokens that are being sold by your own characters.");
		
		addCallbackData("createStoreItem", HtmlComponents.generateStoreItemHtml(db,character, storeCharacter,item,saleItem,request));
		
		if ("Sold".equals(saleItem.getProperty("status")))
			throw new UserErrorMessage("The owner of the store has already sold this item.");
		if ("Hidden".equals(saleItem.getProperty("status")))
			throw new UserErrorMessage("The owner of the store is not selling this item at the moment.");

		Long cost = (Long)saleItem.getProperty("dogecoins");
		if (cost==null)
			throw new UserErrorMessage("The sale item is not setup properly. It has no cost.");
		
		// Handle purchase of quantity here.
		Long quantity = (Long)item.getProperty("quantity");
		if(quantity == null) quantity = 1L;
		if(buyQuantity > quantity)
			throw new UserErrorMessage("Cannot purchase more quantity than the seller is offering.");
		
		CachedEntity sellingCharacter = db.getEntity((Key)saleItem.getProperty("characterKey"));
		if (ODPDBAccess.CHARACTER_MODE_MERCHANT.equals(sellingCharacter.getProperty("mode"))==false)
			throw new UserErrorMessage("The owner of the store is not selling at the moment.");
		if (isPremiumMembership==false && GameUtils.equals(sellingCharacter.getProperty("locationKey"), character.getProperty("locationKey"))==false)
			throw new UserErrorMessage("You are not in the same location as the seller. You can only buy from a merchant who is in the same location as you.");

		if (GameUtils.equals(character.getKey(), sellingCharacter.getKey()))
			throw new UserErrorMessage("You cannot buy items from yourself.");
		
		Double storeSale = (Double)sellingCharacter.getProperty("storeSale");
		if (storeSale==null) storeSale = 100d;

		Long unitCost = Math.round(cost.doubleValue() * (storeSale / 100));
		cost = unitCost * buyQuantity;
		
		if (cost>(Long)character.getProperty("dogecoins"))
			throw new UserErrorMessage("You do not have enough funds to buy this item. You have "+character.getProperty("dogecoins")+" and it costs "+cost+".");	
		if (((Key)item.getProperty("containerKey")).getId()!=sellingCharacter.getKey().getId())
			throw new UserErrorMessage("The item you tried to buy is not actually in the seller's posession. Purchase has been cancelled.");
		
		
		if (cost<0)
			throw new UserErrorMessage("You cannot buy a negatively priced item.");
		
		ds.beginTransaction();
		try
		{
			// Refetch all items in transaction.
			ds.refetch(Arrays.asList(item, saleItem, sellingCharacter, character));
			
			// If quantity item, need to make sure enough 
			// quantity still available to purchase.
			if(quantity > 1)
			{
				quantity = (Long)item.getProperty("quantity");
				if(quantity == null) quantity = 1L;
				if(buyQuantity > quantity)
				{
					// Force reload of the popup so the quantity is updated.
					setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
					throw new UserErrorMessage("Seller no longer has enough inventory to purchase (buying " + buyQuantity + ", seller has " + quantity + ")");
				}
			}

			// Sanitized quantity already. If we're buying less than
			// the item stack quantity, we need to create a new SaleItem 
			// record of the purchased amount.
			if(buyQuantity < quantity)
			{
				// newItem is the purchased item, and will be the purchased quantity.
				// newSaleItem is the sale record.
				CachedEntity newItem = new CachedEntity(item.getKind(), ds.getPreallocatedIdFor(item.getKind()));
				CachedEntity newSaleItem = new CachedEntity(saleItem.getKind(), ds.getPreallocatedIdFor(saleItem.getKind()));
				CachedDatastoreService.copyFieldValues(item, newItem);
				CachedDatastoreService.copyFieldValues(saleItem, newSaleItem);
				
				item.setProperty("quantity", quantity - buyQuantity);
				newSaleItem.setProperty("itemKey", newItem.getKey());
				newItem.setProperty("quantity", buyQuantity);
				ds.put(item);
				item = newItem;
				saleItem = newSaleItem;
			}
			
			// Maintain unit cost instead, in case of quantity items.
			saleItem.setProperty("dogecoins", unitCost);
			saleItem.setProperty("soldPrice", cost);
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

			addCallbackData("createStoreItem", HtmlComponents.generateStoreItemHtml(db,character, storeCharacter,item,saleItem,request));
			
			db.sendMainPageUpdateForCharacter(ds, sellingCharacter.getKey(), "updateMoney");
			db.sendSoundEffectToCharacter(ds, sellingCharacter.getKey(), "coins1");
			db.sendGameMessage(ds, storeCharacter, "You sold "+buyQuantity+" "+item.getProperty("name")+" to "+character.getProperty("name")+" for "+cost+" gold.");
			
			MainPageUpdateService mpus = MainPageUpdateService.getInstance(db, db.getCurrentUser(), character, null, this);
			mpus.updateMoney();
		}
		finally
		{
			ds.rollbackIfActive();
		}
	}
}