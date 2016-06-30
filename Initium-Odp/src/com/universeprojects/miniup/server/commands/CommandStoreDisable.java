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

public class CommandStoreDisable extends Command {
	
	public CommandStoreDisable(HttpServletRequest request, HttpServletResponse response)
	{
		super(request, response);
	}
	
	@Override
public void run(Map<String,String> parameters) throws UserErrorMessage {
		
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter(request);
		
		if ("MERCHANT".equals(character.getProperty("mode")))
		{
			db.setCharacterMode(ds, character, ODPDBAccess.CHARACTER_MODE_NORMAL);
			db.doCharacterTimeRefresh(ds, character);
		}
	}
}