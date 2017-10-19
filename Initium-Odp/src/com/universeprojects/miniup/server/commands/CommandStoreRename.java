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
	
	public CommandStoreRename(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}
	
	@Override
public void run(Map<String,String> parameters) throws UserErrorMessage {
		
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();
		String storeName = parameters.get("name");

		if (storeName==null || storeName.matches(db.STORE_NAME_REGEX)==false)
			throw new UserErrorMessage("The name you provided has invalid characters. Please use only alpha numeric characters or the following symbols: - _/!?+:*&'.,%\"~");
		if (db.checkStoreNameUnique(storeName)==false)
			throw new UserErrorMessage("The store name '"+storeName+"' is already in use. Please choose another.");
		
		if (storeName.length()>160)
			throw new UserErrorMessage("The store name cannot be more than 160 characters long.");
		
		
		character.setProperty("storeName", storeName);
		
		ds.put(character);
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
	}
}
	