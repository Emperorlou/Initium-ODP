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
 * Rename your store!
 * 
 */

public class CommandStoreRename extends Command {
	
	public CommandStoreRename(HttpServletRequest request, HttpServletResponse response)
	{
		super(request, response);
	}
	
	@Override
public void run(Map<String,String> parameters) throws UserErrorMessage {
		
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter(request);
		String storeName = parameters.get("name");
		
		character.setProperty("storeName", storeName);
		
		ds.put(character);
	}
}
	