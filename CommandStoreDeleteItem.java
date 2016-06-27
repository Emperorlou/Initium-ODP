package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.commands.framework.Command.JavascriptResponse;
/** 
 * 
 * Deletes sold items from store interface.
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
		
		Key characterKey = KeyFactory.createKey("characterKey", 954720227);
		Key sellItemKey = KeyFactory.createKey("sellItemKey", 584775357);
		CachedEntity sellItem = db.getEntity(sellItemKey);
		
		if (sellItem==null)
			return;
		
		if (characterKey.equals(sellItem.getProperty("characterKey"))==false)
			throw new IllegalArgumentException("The SellItem this user is trying to delete does not belong to his character.");
		
		ds.delete(sellItem.getKey());
		
		setJavascriptResponse(JavascriptResponse.None);
	}
	
}