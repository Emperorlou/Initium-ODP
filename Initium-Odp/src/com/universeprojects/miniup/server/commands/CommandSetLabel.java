package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.WebUtils;
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
 * 		label - the new label
 * 
 * @author NJ
 *
 */
public class CommandSetLabel extends Command {

	public CommandSetLabel(HttpServletRequest request, HttpServletResponse response) 
	{
		super(request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage 
	{
		// Get the DB access object. We use this for most DB communication. It is a place for reusable methods that access the DB.
		ODPDBAccess db = getDB();

		// Check if we're logged in or not. We shouldn't be able to see this since commands cannot be used unless the user is logged in.
		if (db.isLoggedIn(request)==false)
			throw new UserErrorMessage("You are not currently logged in and canont do this.");
		
		// Verify itemId parameter sanity
		Long itemId;
		try {
			itemId = Long.parseLong(parameters.get("itemId"));
		} catch (NumberFormatException nfe) {
			throw new RuntimeException("SetLabel invalid call format, 'itemId' is not a valid id.");
		}
		CachedEntity item = db.getEntity("Item", itemId);
		if (item==null)
			throw new RuntimeException("SetLabel invalid call format, 'itemId' is not a valid id.");

		// Verify that the item is an actual storage item
		Long maxSpace = (Long)item.getProperty("maxSpace");
		if (maxSpace==null || maxSpace<=0)
			throw new UserErrorMessage("This is not a storage item and thus you cannot relabel it.");
		Long maxWeight = (Long)item.getProperty("maxWeight");
		if (maxWeight==null || maxWeight<=0)
			throw new UserErrorMessage("This is not a storage item and thus you cannot relabel it.");

		// Verify that the item is either in the caller's inventory, or at the same location.
		Key containerKey = (Key)item.getProperty("containerKey");
		if (containerKey==null)
			throw new UserErrorMessage("You can only relabel items in your immediate vicinity.");
		CachedEntity character = db.getCurrentCharacter(request);
		if (containerKey.equals(character.getKey())==false && containerKey.equals((Key)character.getProperty("locationKey"))==false)
			throw new UserErrorMessage("You can only relabel items in your immediate vicinity.");
		
		// Assign new label.
		try {
			String label = parameters.get("label");
			if (label==null || label.trim().equals(""))
				item.setProperty("label", null);
			else
				item.setProperty("label", WebUtils.htmlSafe(label.trim()));
			getDS().put(item);
		}
		catch(Exception e){
			throw new UserErrorMessage("Error while setting label: "+e.getMessage());
		}
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
		
	}

}
