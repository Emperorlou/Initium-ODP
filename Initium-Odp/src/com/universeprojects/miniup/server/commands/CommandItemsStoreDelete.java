package com.universeprojects.miniup.server.commands;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.HtmlComponents;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

/**
 * Extension of the CommandsItemBase abstract class.
 * Deletes the specified items from the user's storefront.
 * 
 * @author SPFiredrake
 * 
 */
public class CommandItemsStoreDelete extends CommandItemsBase {

	public CommandItemsStoreDelete(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}
	
	@Override
	protected void processBatchItems(Map<String, String> parameters,
			ODPDBAccess db, CachedDatastoreService ds, CachedEntity character,
			List<CachedEntity> batchItems) throws UserErrorMessage {
		// TODO Auto-generated method stub
		Key characterKey = (Key) character.getProperty("characterKey");
		StringBuilder storeString = new StringBuilder();
		for(CachedEntity storeItem:batchItems)
		{
			if (GameUtils.equals(characterKey, storeItem.getProperty("characterKey"))==false)
				continue;
			
			ds.delete(storeItem.getKey());
			
			if ("Sold".equals(storeItem.getProperty("status"))==false)
			{
				storeString.append(HtmlComponents.generateInvItemHtml(storeItem));
				processedItems.add(storeItem.getKey().getId());
			}
		}
		
		addCallbackData("createInvItem", storeString.toString());
	}

	protected String getEntityType()
	{
		return "SaleItem";
	}
}
