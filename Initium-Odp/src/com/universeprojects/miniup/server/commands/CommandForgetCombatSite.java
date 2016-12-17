package com.universeprojects.miniup.server.commands;


import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.WebUtils;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.CombatService;
import com.universeprojects.miniup.server.services.MainPageUpdateService;

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
		CachedEntity path = db.getPathById(tryParseId(parameters, "pathId"));

		db.doDeleteCombatSite(null, db.getCurrentCharacter(), path.getKey());		
		
		MainPageUpdateService mpus = new MainPageUpdateService(db, db.getCurrentUser(), character, (CachedEntity)character.getProperty("locationKey"), this);
		mpus.updateButtonList(new CombatService(db));
	}
}
