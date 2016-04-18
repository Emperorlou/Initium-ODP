package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

/**
 * Makes it possible to execute custom scripts attached to an item.
 * 
 * 
 * @author aboxoffoxes
 *
 */
public class CommandExecuteItemScript extends Command {

	public CommandExecuteItemScript(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		ODPDBAccess db = getDB();
		
		// Parameter sanity checking
		Long itemId;
		try
		{
			itemId = Long.parseLong(parameters.get("itemId")); 
		}
		catch (RuntimeException _)
		{
			throw new RuntimeException("ExecuteItemScript invalid call format, itemId '"+parameters.get("itemId")+"' is not a valid id.");
		}
		
		CachedEntity item = db.getEntity("item", itemId);
		String itemScript = (String)item.getProperty("ItemScript");
		if (itemScript == null || itemScript.isEmpty())
		{
			throw new UserErrorMessage("But nothing happened.", false);
		}

		// Executes the item script using RhinoJS
	}
}
