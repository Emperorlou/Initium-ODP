package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.MainPageUpdateService;

/**
 * Deposits the specified gold amount to the specified container object/item ID.
 * 
 * Parameters:
 * 		itemId - Item ID of the container object depositing to 
 * 		amount - The amount to deposit
 * 
 * @author SPFiredrake
 *
 */
public class CommandDogeCoinsDepositToItem extends Command {

	public CommandDogeCoinsDepositToItem(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
	}
	
	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();
		
		long itemId = tryParseId(parameters, "itemId");
		CachedEntity item = db.getEntity("Item", itemId);
		if(item == null)
			throw new UserErrorMessage("Item does not exist");
		
		long depositAmount = tryParseId(parameters, "amount");
		if(depositAmount < 0)
			throw new UserErrorMessage("Cannot deposit a negative amount");

		Long characterCoins = (Long)character.getProperty("dogecoins");
		if(depositAmount > characterCoins)
			throw new UserErrorMessage("Character does not have the specified coins to deposit");
		
		CachedEntity itemContainer = db.getEntity((Key)item.getProperty("containerKey"));
		if(itemContainer == null)
			throw new RuntimeException("itemId " + itemId + " does not have a valid container");

		if(db.checkContainerAccessAllowed(character, itemContainer)==false)
			throw new UserErrorMessage("Character does not have access to this container");

		Long containerCoins = (Long)item.getProperty("dogecoins");
		character.setProperty("dogecoins", characterCoins - depositAmount);
		item.setProperty("dogecoins", containerCoins + depositAmount);
		
		// Order matters! Always subtract coins first!
		ds.put(character);
		ds.put(item);
		
		MainPageUpdateService service = new MainPageUpdateService(db, this);
		service.updateMoney(character);
	}

}
