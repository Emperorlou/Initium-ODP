package com.universeprojects.miniup.server.commands;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.HtmlComponents;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class CommandEatBerry extends Command {
	
	public CommandEatBerry(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}
	
	public void run(Map<String, String> parameters) throws UserErrorMessage 
	{
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		
		CachedEntity character = db.getCurrentCharacter(request);
		Long itemId = tryParseId(parameters,"itemId");		
		CachedEntity item = db.getEntity("item",itemId);
		
		if (item==null)
			throw new UserErrorMessage("Item doesn't exist.");
		
		if (GameUtils.equals(item.getProperty("containerKey"),character.getKey())==false)
			throw new UserErrorMessage("You cannot eat this berry. It must be in your inventory!");
		if(db.awardBuff_Berry(ds,character)==false)
			throw new UserErrorMessage("Only one berry buff can be active at a time.");
			ds.delete(item);
	}
}
	
	