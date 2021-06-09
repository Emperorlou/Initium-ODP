package com.universeprojects.miniup.server.commands;


import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.WebUtils;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

/**
 * Allows the player to attempt to forget a combat site.
 * (press the x next to a combat site)
 * 
 * @author tacobowl8
 * 
 */
public class CommandForgetCombatSite extends Command {
	
	public CommandForgetCombatSite(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
	}
	
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		ODPDBAccess db = getDB();
		CachedEntity character = db.getCurrentCharacter();
		
		if(CommonChecks.checkCharacterIsZombie(character))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");
		
		Long locationId = WebUtils.getLongParam(request, "locationId");

		db.doDeleteCombatSite(null, character, KeyFactory.createKey("Location", locationId), true, false);		
		
		deleteHtml(".location-link-"+locationId);
	}
}
