package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
/** 
 * 
 * Sell an Item!
 * 
 */

public class CommandStoreSetSale extends Command {
	
	public CommandStoreSetSale(HttpServletRequest request, HttpServletResponse response)
	{
		super(request, response);
	}
	
	@Override
public void run(Map<String,String> parameters) throws UserErrorMessage {
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter(request);
		
		Double sale = Double.parseDouble(parameters.get("sale"));

		if (ds.flagActionLimiter("saleChangeLimiter-" + character.getKey().getId(), 600, 2))
			throw new UserErrorMessage("You are trying to change the sale settings for your store too often. Try again in about 10 minutes.");

		if (sale < 0) throw new UserErrorMessage("Sales can not be negative.");

		character.setProperty("storeSale", sale);
		
		ds.put(character);
	}
}