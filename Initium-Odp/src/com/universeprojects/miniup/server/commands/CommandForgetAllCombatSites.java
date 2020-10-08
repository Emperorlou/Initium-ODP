package com.universeprojects.miniup.server.commands;


import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.CombatService;
import com.universeprojects.miniup.server.services.MainPageUpdateService;

/**
 * Allows the player to attempt to forget all combat sites at their location.
 * 
 * @author tacobowl8
 * 
 */
public class CommandForgetAllCombatSites extends Command {
	
	private static final long MAX_MILLISECONDS_TO_SPEND = 10000;
	
	public CommandForgetAllCombatSites(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
	}
	
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		long startTime = System.currentTimeMillis();
		int numberOfSitesForgotten = 0;
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		ds.beginBulkWriteMode();

		CachedEntity character = db.getCurrentCharacter();
		
		if(CommonChecks.checkCharacterIsZombie(character))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");
		
		List<Long> forgettableCombatSiteList = tryParseStringToLongList(parameters, "forgettableCombatSiteArray", ",");
		//The location the command is being called from
		Key characterLocationKey = (Key)character.getProperty("locationKey");
		try 
		{
			for(Long forgettableCombatSite : forgettableCombatSiteList) {
				if(System.currentTimeMillis() - startTime >= MAX_MILLISECONDS_TO_SPEND)
					throw new UserErrorMessage("The bulk forgetting of sites has stopped due to it taking a while.  A total of "+numberOfSitesForgotten+" sites were forgotten.", false);
				db.doDeleteCombatSite(ds, character, KeyFactory.createKey("Location", forgettableCombatSite), true, false);
				numberOfSitesForgotten++;
			}
		} 
		catch (UserErrorMessage e) 
		{
			// Only throw if it's an error, since it clears any ajax updates from the command.
			if(e.isError())
				throw e;
			else
				setPopupMessage(e.getMessage());
		} 
		finally 
		{
			ds.commitBulkWrite();
			MainPageUpdateService mpus = MainPageUpdateService.getInstance(db, db.getCurrentUser(), character, db.getLocationById(characterLocationKey.getId()), this);
			mpus.updateButtonList();
		}
		
		setJavascriptResponse(JavascriptResponse.ReloadMiniPagePopup);
	}
}
