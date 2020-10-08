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
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
/** 
 * 
 * Delete an item from store interface.
 * 
 */

public class CommandStoreDeleteItem extends Command {
	
	public CommandStoreDeleteItem(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}
	
	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage 
	{
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		
		Long saleItemId = Long.parseLong(parameters.get("saleItemId"));
		
		Key saleItemKey = KeyFactory.createKey("SaleItem", saleItemId);
		CachedEntity saleItem = db.getEntity(saleItemKey);
		if (saleItem==null)
			return;
		
		CachedEntity user = db.getCurrentUser();
		Key characterKey = db.getCurrentCharacterKey();
		
		if(CommonChecks.checkCharacterIsZombie(db.getCurrentCharacter()))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");
		
		CachedEntity item = db.getEntity((Key) saleItem.getProperty("itemKey"));
		
		
		if (GameUtils.equals(characterKey, saleItem.getProperty("characterKey"))==false)
			throw new UserErrorMessage("The SaleItem this user is trying to delete does not belong to his character.");
		
		ds.delete(saleItem.getKey());
		
		if ("Sold".equals(saleItem.getProperty("status"))==false)
			addCallbackData("createInvItem", HtmlComponents.generateInvItemHtml(item));
	}
	
}