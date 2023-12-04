package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.TransactionCommand;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.MainPageUpdateService;

public class CommandStoreBuyOrderExecute extends TransactionCommand
{

	public CommandStoreBuyOrderExecute(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}

	@Override
	public void runInsideTransaction(Map<String, String> parameters) throws UserErrorMessage
	{
		Long buyOrderId = Long.parseLong(parameters.get("buyOrderId"));
		String[] itemIds = request.getParameterValues("itemId[]");
		
		int totalEntitiesBought = 0;
		Long totalQuantityBought = 0L;
		Long totalBoughtGold = 0L;
		boolean storeRanLowOfFunds = false;
		
		CachedEntity character = db.getCurrentCharacter();


		// Prepare the buy order
		CachedEntity buyOrder = db.getEntity("BuyItem", buyOrderId);
		if (buyOrder==null) throw new UserErrorMessage("This buy order no longer exists.");
		Long maxQuantity = (Long)buyOrder.getProperty("quantity");
		Long costEach = (Long)buyOrder.getProperty("value");
		String itemName = (String)buyOrder.getProperty("name");
		
		// Prepare the store owner
		Key storeCharacterKey = (Key)buyOrder.getProperty("characterKey");
		CachedEntity storeCharacter = db.getEntity(storeCharacterKey);
		if (storeCharacter==null) throw new UserErrorMessage("This store owner no longer exists.");
		if (CommonChecks.checkCharacterIsVending(storeCharacter)==false) throw new UserErrorMessage("The store owner has closed his store.");
		if (GameUtils.equals(storeCharacter.getProperty("locationKey"), character.getProperty("locationKey"))==false)
			throw new UserErrorMessage("You are not in the same location as the character you're trying to sell to.");
		

		
		// Prepare the buyer (us)
		character.refetch(ds);
		
		if(CommonChecks.checkCharacterIsZombie(character))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");
		
		if (CommonChecks.checkCharacterIsBusy(character)) throw new UserErrorMessage("You're busy, you can't do this right now.");
		
		for(int i = 0; i<itemIds.length; i++)
		{
			
			Long itemId = Long.parseLong(itemIds[i]);
			
	
			// Prepare the item to sell
			CachedEntity item = db.getEntity("Item", itemId);
			if (item==null) throw new UserErrorMessage("Item does not exist.");
			if (CommonChecks.checkItemIsInCharacterInventory(item, db.getCurrentCharacterKey())==false) throw new UserErrorMessage("Item is not in your inventory.");
			if (CommonChecks.checkItemIsCustomized(item)) throw new UserErrorMessage("You cannot sell customized items this way.");
			Long availableQuantity = 1L;
			if (item.getProperty("quantity")!=null)
				availableQuantity = (Long)item.getProperty("quantity");
			
			
			
			if (GameUtils.equals(itemName, item.getProperty("name"))==false) throw new UserErrorMessage("The item you're trying to sell is not suitable for this buy order.");
			
			
			if (CommonChecks.checkItemIsEquipped(item.getKey(), db.getCurrentCharacter()))
				throw new UserErrorMessage("You cannot sell an item that you currently have equipped.");
			
			Long storeGold = (Long)storeCharacter.getProperty("dogecoins");
	
			if (storeGold<costEach)
				if (totalEntitiesBought==0)
					throw new UserErrorMessage(""+storeCharacter.getProperty("name")+" does not have enough funds for this transaction. Maybe try again later.");
				else
					break;
			
			Long actualQuantityToSell = Long.MAX_VALUE;
			if (maxQuantity!=null)
				actualQuantityToSell = maxQuantity;
			if (availableQuantity<actualQuantityToSell)
				actualQuantityToSell = availableQuantity;
			if (costEach*actualQuantityToSell>storeGold)
			{
				storeRanLowOfFunds = true;
				actualQuantityToSell = storeGold/costEach;
			}
			
			if (actualQuantityToSell<=0)
				if (totalEntitiesBought==0)
					throw new UserErrorMessage("The store owner does not want any more.");
				else
					break;
			
			Long yourGold = (Long)db.getCurrentCharacter().getProperty("dogecoins");
			
			
			Long totalCost = actualQuantityToSell*costEach;
			
			if (item.getProperty("quantity")==null || GameUtils.equals(actualQuantityToSell, item.getProperty("quantity")))
			{
				storeGold -= totalCost;
				yourGold += totalCost;
				
				
				item.setProperty("containerKey", storeCharacter.getKey());
				ds.put(item);
			}
			else
				throw new UserErrorMessage("Currently we can only process complete stacks (or non-stacked items). Sorry for the inconvenience! In the meantime, manually split your stack of items if you want to sell it and make sure it has only up to "+actualQuantityToSell+" units.");
			
			storeCharacter.setProperty("dogecoins", storeGold);
			db.getCurrentCharacter().setProperty("dogecoins", yourGold);
			
			
			totalBoughtGold+=totalCost;
			totalQuantityBought+=actualQuantityToSell;
			
			totalEntitiesBought++;

			if (maxQuantity!=null)
				buyOrder.setProperty("quantity", maxQuantity-totalQuantityBought);
			
			
			if (totalEntitiesBought>=20)
				break;
		}

		ds.put(storeCharacter, db.getCurrentCharacter(), buyOrder);
		
		String msg = "You sold "+totalQuantityBought+" "+itemName+" to "+storeCharacter.getProperty("name")+" totalling "+GameUtils.formatNumber(totalBoughtGold)+"g.";
		if (storeRanLowOfFunds)
			msg+=" The store ran low on funds and so the number of items sold was reduced.";
		db.sendGameMessage(ds, db.getCurrentCharacter(), msg);
		
		
		
		String storeMsg = "You bought "+totalQuantityBought+" "+itemName+" from "+db.getCurrentCharacter().getProperty("name")+" totalling "+GameUtils.formatNumber(totalBoughtGold)+"g.";
		if (storeRanLowOfFunds)
			msg+=" Your store ran low on funds and so the number of items bought was reduced.";
		db.sendGameMessage(ds, storeCharacter, storeMsg);

		
		
		db.sendMainPageUpdateForCharacter(ds, storeCharacterKey, "updateMoney");
		MainPageUpdateService mpus = MainPageUpdateService.getInstance(db, db.getCurrentUser(), db.getCurrentCharacter(), null, this);
		mpus.updateMoney();
		
	}


}
