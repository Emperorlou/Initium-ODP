package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.HtmlComponents;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.MainPageUpdateService;
import com.universeprojects.miniup.server.commands.framework.Command.JavascriptResponse;

/**
 * Toggle storefront command.
 * Based on CommandStoreEnable and CommandStoreDisable.
 * 
 * @author SPFiredrake
 * 
 */

public class CommandToggleStorefront extends Command 
{

	/**
	 * Command to toggle storefront, enabling or disabling based on current character entity state
	 * 
	 * @param request
	 *            Server request
	 * @param response
	 *            Server response
	 */
	public CommandToggleStorefront(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}
	
	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		// TODO Auto-generated method stub
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();
		
//		if ("MarketSite".equals(characterLocation.getProperty("type"))==false)
//			throw new UserErrorMessage("You cannot setup shop outside of a marketplace.");
		
		if ("MERCHANT".equals(character.getProperty("mode")))
		{
			db.setCharacterMode(ds, character, ODPDBAccess.CHARACTER_MODE_NORMAL);
		}
		else
		{
			if ("COMBAT".equals(character.getProperty("mode")))
				throw new UserErrorMessage("You cannot setup shop while in combat.");
			
			db.setCharacterMode(ds, character, ODPDBAccess.CHARACTER_MODE_MERCHANT);
		}
		
		db.doCharacterTimeRefresh(ds, character);	// This is saving the character, so no need to save after this
		
		if (parameters.get("buttonId")==null || parameters.get("buttonId").equals("") ||  parameters.get("buttonId").equals("undefined") || parameters.get("buttonId").equals("null"))
		{
			MainPageUpdateService mpus = new MainPageUpdateService(db, db.getCurrentUser(), character, db.getCharacterLocation(character), this);
			mpus.updateButtonBar();
		}
		else
			updateHtml("#"+parameters.get("buttonId"), HtmlComponents.generateToggleStorefront(character));
		
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
	}

}
