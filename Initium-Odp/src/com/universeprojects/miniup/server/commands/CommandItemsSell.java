package com.universeprojects.miniup.server.commands;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.HtmlComponents;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

/**
 * Extension of the CommandsItemBase abstract class.
 * Sells all the specified items for the amount indicated by user.
 * Skips over Premium Membership tokens. 
 * 
 * @author SPFiredrake
 * 
 */
public class CommandItemsSell extends CommandItemsBase {

	public CommandItemsSell(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}
	
	@Override
	protected void processBatchItems(Map<String, String> parameters, ODPDBAccess db, CachedDatastoreService ds,
			CachedEntity character, List<CachedEntity> batchItems)
			throws UserErrorMessage {
		// TODO Auto-generated method stub
		Long amount = Long.parseLong(parameters.get("amount"));
		
		if (amount<0)
			throw new UserErrorMessage("You cannot sell items for less than 0 gold.");
		
		StringBuilder saleString = new StringBuilder();
		for(CachedEntity batchSale:batchItems)
		{
			// Skip premium membership tokens. This was a conscious decision, as we
			// want players to be mindful of token prices.
			if ("Initium Premium Membership".equals(batchSale.getProperty("name")))
			{
				setPopupMessage("Premium tokens cannot be sold through this method.");
				continue;
			}
			
			// If item is equipped, belongs to someone else, or is already being sold,
			// then just skip processing.
			Key itemKey = batchSale.getKey();
			if (db.checkCharacterHasItemEquipped(character, itemKey))
				continue;
			
			if (batchSale.getProperty("containerKey").equals(character.getKey())==false)
				continue;
			
			if (db.checkItemBeingSoldAlready(character.getKey(), itemKey))
				continue;
			
			CachedEntity saleItem = db.newSaleItem(ds, character, batchSale, amount);
			saleString.append(HtmlComponents.generateSellItemHtml(db,saleItem,request));
			processedItems.add(saleItem.getKey().getId());
		}
		
		// If no items were sold, then we're returning an empty string, which won't change
		// the HTML in the callback.
		addCallbackData("createSellItem", saleString.toString());
	}

}
