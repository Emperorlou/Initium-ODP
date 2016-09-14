package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.commands.framework.Command.JavascriptResponse;
/** 
 * 
 * Sell an Item!
 * 
 */

public class CommandStoreDisable extends Command {
	
	public CommandStoreDisable(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}
	
	@Override
public void run(Map<String,String> parameters) throws UserErrorMessage {
		
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter(request);
		
		if ("MERCHANT".equals(character.getProperty("mode")))
		{
			addCallbackData(
					"html",
					"<a onclick='storeEnabledNew(event)'  title='Clicking here will disable your storefront so other players cannot buy your goods.'><img src='images/ui/storefrontDisabled.png' border=0/></a>");
			db.setCharacterMode(ds, character, ODPDBAccess.CHARACTER_MODE_NORMAL);
			db.doCharacterTimeRefresh(ds, character);
		}
		else
		{
			addCallbackData(
					"html",
					"<a onclick='storeDisabledNew(event)'  title='Clicking here will enable your storefront so other players can buy your goods.'><img src='images/ui/storefrontEnabled.png' border=0/></a>");
		}
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
	}
}