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
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

/**
 * Extension of the CommandsItemBase abstract class.
 * Adds the specified items to the trade window
 * 
 * @author SPFiredrake
 * 
 */
public class CommandItemsTrade extends CommandItemsBase {

	public CommandItemsTrade(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}
	
	@Override
	protected void processBatchItems(Map<String, String> parameters, 
			ODPDBAccess db, CachedDatastoreService ds,
			CachedEntity character, List<CachedEntity> batchItems)
			throws UserErrorMessage {
		// TODO Auto-generated method stub
		TradeObject tradeObject = TradeObject.getTradeObjectFor(ds, character);
		Key otherCharacter = (Key) character.getProperty("combatant");
		if (tradeObject==null || tradeObject.isCancelled())
			throw new UserErrorMessage("Trade has been cancelled.");
		if (tradeObject.isComplete())
			throw new UserErrorMessage("Trade is already complete.");
		if(otherCharacter == null)
			throw new UserErrorMessage("Not currently trading with other character.");
		
		StringBuilder tradedString = new StringBuilder();
		for(CachedEntity tradeItem:batchItems)
		{
			try
			{
				db.addTradeItem(ds, tradeObject, character, tradeItem);
				tradedString.append(HtmlComponents.generatePlayerTradeItemHtml(tradeItem));
				processedItems.add(tradeItem.getKey().getId());
			}
			catch(UserErrorMessage ex)
			{
				// Ignore. Indicates the trade is cancelled or the item doesn't belong
				// to the current user. We don't generate the HTML if this happens,
				// effectively skipping the trade for this item.
			}
		}
		
		db.sendNotification(ds, otherCharacter, NotificationType.tradeChanged);
        
        Integer tradeVersion = tradeObject.getVersion();
        System.out.println("");
        addCallbackData("tradeVersion",tradeVersion);
        addCallbackData("createTradeItem",tradedString.toString());
	}

}
