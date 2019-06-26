package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class CommandMoveItem extends Command
{

	public CommandMoveItem(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException
	{
		Long itemId = tryParseId(parameters, "itemId");
		String destinationKeyStr = parameters.get("destinationKey");
		String[] parts = destinationKeyStr.split("_");
		Key destinationKey = KeyFactory.createKey(parts[0], Long.parseLong(parts[1]));
		
		CachedEntity item = db.getEntity(KeyFactory.createKey("Item", itemId));
		CachedEntity newContainer = db.getEntity(destinationKey);
		
		db.doMoveItem(this, null, db.getCurrentCharacter(), item, newContainer);
	}

}
