package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;


/**
 * Allows the current leader of a party to set a different party member as leader
 * 
 * Usage notes:
 * Checks if itemId is a valid storage option, and in inventory/at the current location
 * Also sanitises label for any possible injection ideas. 
 * 
 * Parameters:
 * 		itemId - itemID of the chest to be relabelled 
 * 		label - the new label, max 30 char
 * 
 * @author NJ
 *
 */
public class CommandSetLabel extends Command {

	public CommandSetLabel(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) 
	{
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage 
	{
		ODPDBAccess db = getDB();

		// Verify itemId parameter sanity
		CachedEntity item = db.getEntity("Item", tryParseId(parameters, "itemId"));
		if (item==null)
			throw new RuntimeException("SetLabel invalid call format, 'itemId' is not a valid id.");
		
		// Verify label parameter sanity
		String label = parameters.get("label");
		if (label!=null && label.length()>30)
			throw new UserErrorMessage("Labels are limited to a maximum of 30 characters.");

		// Verify that the item is an actual storage item
		if (GameUtils.isStorageItem(item)==false)
			throw new UserErrorMessage("This is not a storage item and thus you cannot relabel it.");

		// Verify that the item is either in the caller's inventory, or at the same location.
		Key containerKey = (Key)item.getProperty("containerKey");
		if (containerKey==null)
			throw new UserErrorMessage("You can only relabel items in your immediate vicinity.");
		CachedEntity character = db.getCurrentCharacter();
		if (GameUtils.equals(containerKey,character.getKey())==false && GameUtils.equals(containerKey,(Key)character.getProperty("locationKey"))==false)
			throw new UserErrorMessage("You can only relabel items in your immediate vicinity.");
		
		// Assign new label.
		try {
			if (label==null || label.trim().equals(""))
				item.setProperty("label", null);
			else
				item.setProperty("label", label.replace("'", "`").trim());
			getDS().put(item);
		}
		catch(Exception e){
			throw new UserErrorMessage("Error while setting label: "+e.getMessage());
		}
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
		
	}

}
