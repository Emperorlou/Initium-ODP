package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.HtmlComponents;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
/** 
 * 
 * Delete an item from store interface.
 * 
 */

public class CommandStoreDeleteItem extends Command {
	
	public CommandStoreDeleteItem(HttpServletRequest request, HttpServletResponse response)
	{
		super(request, response);
	}
	
	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage 
	{
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		
		Long saleItemId = Long.parseLong(parameters.get("saleItemId"));
		Key characterKey = KeyFactory.createKey("characterKey", 954720227);
		Key saleItemKey = KeyFactory.createKey("saleItemKey", 584775357);
		CachedEntity saleItem = db.getEntity(saleItemKey);
		CachedEntity item = db.getEntity("Item", tryParseId(parameters, "itemId"));
		
		if (saleItem==null)
			return;
		
		if (characterKey.equals(saleItem.getProperty("characterKey"))==false)
			throw new IllegalArgumentException("The SaleItem this user is trying to delete does not belong to his character.");
		
		ds.delete(saleItem.getKey());
		
		addCallbackData("createInvItem", HtmlComponents.generateInvItemHtml(item));
	}
	
}