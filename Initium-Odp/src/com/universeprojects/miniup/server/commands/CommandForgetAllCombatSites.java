package com.universeprojects.miniup.server.commands;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
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
	
	public CommandForgetAllCombatSites(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
	}
	
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		ds.beginBulkWriteMode();
		
		CachedEntity character = db.getCurrentCharacter();
		List<Long> forgettableCombatSiteList = tryParseStringToArray(parameters, "forgettableCombatSiteArray", ",");
	
		//The location the command is being called from
		Key characterLocationKey = (Key)character.getProperty("locationKey");

		
		for(Long forgettableCombatSite : forgettableCombatSiteList) {
			db.doDeleteCombatSite(ds, character, KeyFactory.createKey("Location", forgettableCombatSite));
		}
		ds.commitBulkWrite();
		
		MainPageUpdateService mpus = new MainPageUpdateService(db, db.getCurrentUser(), character, db.getLocationById(characterLocationKey.getId()), this);
		mpus.updateButtonList(new CombatService(db));
	}
	
	/**
	 * Tries to fetch fieldName from param list and throws if it cannot.
	 * 
	 * @param params
	 * @param fieldName
	 * @param delimitedCharacter
	 * @return Array, parsed from param string
	 */
	protected List<Long> tryParseStringToArray(Map<String,String> params, String fieldName, String delimitingCharacter)
	{
		try {
			String unparsedArray = params.get(fieldName);
			String[] parsedArray = unparsedArray.split(delimitingCharacter);
			ArrayList<Long> parsedList = new ArrayList<Long>();
			for(int i = 0; i < parsedArray.length; i++) {
				parsedList.add(Long.parseLong(parsedArray[i]));
			}
			return parsedList;
		} catch (Exception _) {
			throw new RuntimeException(this.getClass().getSimpleName()+" invalid call format, '"+fieldName+"' is not a valid delimited list.");
		}
	}
}
