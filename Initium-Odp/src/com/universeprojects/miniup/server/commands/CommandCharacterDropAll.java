package com.universeprojects.miniup.server.commands;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.commands.framework.Command.JavascriptResponse;

public class CommandCharacterDropAll extends Command {

	public CommandCharacterDropAll(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}
	
	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();
		
		// We will add all items that are for sale or equipped to a master ignore list.
		// This should speed up the process by not having to perform these checks for every item
		// Using HashSet, since we want to ignore duplicates (such as multi-slot equipped items)
		HashSet<Long> ignoreItems = new HashSet<Long>();
	
		// Find equipped items first.
		for (String slot:ODPDBAccess.EQUIPMENT_SLOTS)
		{
			Key equipmentInSlot = (Key) character.getProperty("equipment" + slot);
			if (equipmentInSlot != null)
			{
				ignoreItems.add(equipmentInSlot.getId());
			}
		}
		
		// Find all sale items next. We don't care if itemKey references a destroyed item here
		// since it's simply used to see if the item key from inv exists in the ignore list.
		List<CachedEntity> saleItems = db.getSaleItemsFor(character.getKey()); 
		for(CachedEntity item:saleItems)
		{
			Key itemKey = (Key)item.getProperty("itemKey");
			ignoreItems.add(itemKey.getId());
		}
		
		Key characterLocationKey = (Key) character.getProperty("locationKey");
		List<CachedEntity> invItems = db.getFilteredList("Item", "containerKey", character.getKey());
		for(CachedEntity dropItem:invItems)
		{
			if(ignoreItems.contains(dropItem.getKey().getId())) continue;
			
			dropItem.setProperty("containerKey", characterLocationKey);
			dropItem.setProperty("movedTimestamp", new Date());
			ds.put(dropItem);
		}
		
		// Reload the popup to show the modified inventory.
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
	}

}
