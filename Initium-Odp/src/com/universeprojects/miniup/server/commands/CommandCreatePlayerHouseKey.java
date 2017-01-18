package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class CommandCreatePlayerHouseKey extends Command{

	/**
	 * Creates key(this is an item that the character holds) to enter his house,
	 * 
	 * @author Seathninja
	 */
	
	public CommandCreatePlayerHouseKey(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}
	
	public void run(Map<String, String> parameters) throws UserErrorMessage 
	{
		
		//preliminary creating the item into existence
		CachedEntity houseKey = new CachedEntity("Item");
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();
		ds.put(houseKey);
		
		//setting durability and assigning to player inventory here
		if (parameters.get("reusable") == "true")
		{
			//here is giving infi use key to player
			houseKey.setProperty("containerKey", character.getKey());
		}
		else
		{
			//here is making single use key and giving to player
			houseKey.setProperty("durability", 1);
			houseKey.setProperty("maxDurability", 1);
			houseKey.setProperty("containerKey", character.getKey());
			
		}
	}
	
	
	
}
