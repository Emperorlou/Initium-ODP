package com.universeprojects.miniup.server.commands;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
/** 
 * 
 * Deletes sold items from store interface.
 * 
 */

public class CommandStoreDeleteSoldItems extends Command {
	
	public CommandStoreDeleteSoldItems(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}
	
	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage 
	{
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		
		CachedEntity user = db.getCurrentUser();
		Key characterKey = (Key) user.getProperty("characterKey");
				
		List<CachedEntity> saleItems = db.getSaleItemsFor(characterKey);
		
		for(CachedEntity item:saleItems)
		{
			if (item!=null)
			{
				if (characterKey.equals(item.getProperty("characterKey"))==false)
					throw new IllegalArgumentException("The SellItem this user is trying to delete does not belong to his character.");
				
				if (item.getProperty("status")!=null && item.getProperty("status").equals("Sold"))
					ds.delete(item);
			}
		}
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
	}
	
}