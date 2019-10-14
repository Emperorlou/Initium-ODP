package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.MainPageUpdateService;
import com.universeprojects.miniup.server.services.PropertiesService;

public class CommandBuyHouse extends Command {
	public static final long DOGECOIN_COST = 2000;

	public CommandBuyHouse(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
	}

	public void run(Map<String, String> parameters) throws UserErrorMessage {
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();

		CachedEntity user = db.getCurrentUser();
		CachedEntity character = db.getCurrentCharacter();

		CachedEntity currentLocation = db.getEntity((Key) character.getProperty("locationKey"));
		String houseName = db.cleanCharacterName(parameters.get("houseName"));
		
		PropertiesService ps = new PropertiesService(db);
		CachedEntity newHouse = ps.buyHouse(db, ds, user, character, currentLocation, houseName, DOGECOIN_COST);
		if(newHouse != null)
		{
			character.setProperty("locationKey", newHouse.getKey());
			
			getQuestService().checkLocationForObjectiveCompletions(newHouse);
			getQuestService().checkCharacterPropertiesForObjectiveCompletions();
			
			MainPageUpdateService mpus = MainPageUpdateService.getInstance(db, user, character, newHouse, this);
			mpus.updateFullPage_shortcut();
			db.sendGameMessage(ds, character, "You have purchased a house!");
			
		}
	}
}
