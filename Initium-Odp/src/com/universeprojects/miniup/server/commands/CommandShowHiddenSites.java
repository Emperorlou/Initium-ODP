package com.universeprojects.miniup.server.commands;


import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.CombatService;
import com.universeprojects.miniup.server.services.MainPageUpdateService;

/**
 * Shows the hidden sites at the character's location.
 * 
 * @author tacobowl8
 * 
 */
public class CommandShowHiddenSites extends Command {
	
	public CommandShowHiddenSites(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
	}
	
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		ODPDBAccess db = getDB();
		CachedEntity character = db.getCurrentCharacter();

		MainPageUpdateService mpus = new MainPageUpdateService(db, db.getCurrentUser(), character, db.getLocationById(((Key) character.getProperty("locationKey")).getId()), this);
		mpus.updateButtonList(new CombatService(db), true);
	}
}
