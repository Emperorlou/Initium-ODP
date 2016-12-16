package com.universeprojects.miniup.server.commands;

import java.util.ArrayList;
import java.util.HashMap;
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
		
		List<Key> invItemKeys = new ArrayList<Key>();
		for(CachedEntity storeItem:batchItems)
			if (storeItem!=null)
				invItemKeys.add((Key)storeItem.getProperty("itemKey"));
		
		Map<CachedEntity, CachedEntity> storeToItemsMap = new HashMap<CachedEntity, CachedEntity>();
		List<CachedEntity> invItems = ds.get(invItemKeys);
		for(CachedEntity storeItem:batchItems)
		{
			for(CachedEntity item:invItems)
			{
				if(item != null && GameUtils.equals(item.getKey(), storeItem.getProperty("itemKey")))
				{
					storeToItemsMap.put(storeItem, item);
					break;
				}
			}
		}
		
		Key characterKey = character.getKey();
		StringBuilder storeString = new StringBuilder();
		for(CachedEntity storeItem:batchItems)
		{
			if (storeItem==null) continue;
			
			if (GameUtils.equals(characterKey, storeItem.getProperty("characterKey"))==false)
				continue;
			
			ds.delete(storeItem.getKey());
			if ("Sold".equals(storeItem.getProperty("status"))==false && storeToItemsMap.containsKey(storeItem))
			{
				CachedEntity item = storeToItemsMap.get(storeItem);
				storeString.append(HtmlComponents.generateInvItemHtml(item));
			}
			
			// Add no matter what, so we can remove from the original list.
			processedItems.add(storeItem.getKey().getId());
		}
		
		addCallbackData("createInvItem", storeString.toString());
	}
	
	@Override
	protected String getEntityType()
	{
		return "SaleItem";
	}
}
