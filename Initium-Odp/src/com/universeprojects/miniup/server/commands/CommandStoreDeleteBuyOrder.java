package com.universeprojects.miniup.server.commands;

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
/** 
 * 
 * Delete an item from store interface.
 * 
 */

public class CommandStoreDeleteBuyOrder extends Command {
	
	public CommandStoreDeleteBuyOrder(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}
	
	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage 
	{
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		
		Long buyOrderId = Long.parseLong(parameters.get("buyOrderId"));
		
		Key buyOrderKey = KeyFactory.createKey("BuyItem", buyOrderId);
		CachedEntity buyOrder = db.getEntity(buyOrderKey);
		if (buyOrder==null)
			return;
		
		CachedEntity user = db.getCurrentUser();
		Key characterKey = db.getCurrentCharacterKey();
		
		if (GameUtils.equals(characterKey, buyOrder.getProperty("characterKey"))==false)
			throw new UserErrorMessage("The buy order this user is trying to delete does not belong to his character.");
		
		ds.delete(buyOrder.getKey());

		deleteHtml(".buyOrder[ref="+buyOrderId+"]");
	}
	
}