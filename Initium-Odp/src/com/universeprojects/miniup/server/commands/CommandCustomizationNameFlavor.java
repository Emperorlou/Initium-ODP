package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.WebUtils;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class CommandCustomizationNameFlavor extends Command
{
	final String regexValidator = "[A-Za-z0-9'\", \\-\\(\\)/\\.?!&$%#\r\n]+";
	public CommandCustomizationNameFlavor(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException
	{
		Long itemId = WebUtils.getLongParam(request, "itemId");
		CachedEntity entity = db.getEntity("Item", itemId);
		
		if (entity.getProperty("quantity")!=null)
			throw new UserErrorMessage("You cannot customize a stackable item.");
		
		// This is the Custom Item - Name and Flavor Text order type
		Key typeKey = KeyFactory.createKey("CustomOrderType", 5663994663665664L);
		
		String itemName = parameters.get("itemName");
		String flavor = parameters.get("flavor");
		
		itemName = itemName.trim().replace("  ", " ");
		flavor = flavor.trim().replace("  ", " ");
		
		// Verify this is a name we can use...
		if (itemName.matches(regexValidator)==false)
			throw new UserErrorMessage("Item can only contain the following special characters: ',.-()/");
		
		if (flavor.matches(regexValidator)==false)
			throw new UserErrorMessage("Item flavor can only contain the following special characters: ',.-()/");
		
		if (itemName.length()<1)
			throw new UserErrorMessage("Item name must not be blank.");
		
		if (itemName.length()>30)
			throw new UserErrorMessage("Item name cannot be longer than 30 characters.");
		
		if (flavor.length()>40000)
			throw new UserErrorMessage("Flavor cannot be longer than 40,000 characters.");
		
		flavor = flavor.replace("\n", "<br>");
		

		
		// Creating a description
		String description = "Item Name: "+itemName+" - Flavor: "+flavor;
		
		// This is mostly just done for tracking purposes
		db.newCustomOrder(db.getCurrentUser(), db.getCurrentCharacter(), entity, typeKey, description, true);
		
		entity.setProperty("name", itemName);
		entity.setProperty("description", flavor);
		entity.setProperty("forcedItemQuality", "Custom");
		
		db.getDB().put(entity);

		setPopupMessage("Your order has been completed automatically.");
		
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
	}

}
