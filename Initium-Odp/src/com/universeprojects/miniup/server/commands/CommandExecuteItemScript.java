package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.commands.jswrappers.ODPDBAccessWrapper;

/**
 * Makes it possible to execute script associated with an item.
 * 
 * Keep in mind that this command is meant to be safe: if you add more variables to the script scope,
 * make sure to keep it secret, keep it safe.
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

		// Safely fetch itemId
		Long itemId;
		try
		{
			itemId = Long.parseLong(parameters.get("itemId")); 
		}
		catch (RuntimeException _)
		{
			throw new RuntimeException("ExecuteItemScript invalid call format, itemId '"+parameters.get("itemId")+"' is not a valid id.");
		}

		// Retrieve item by id and ensure a script is attached to it
		CachedEntity item = db.getEntity("item", itemId);
		String itemScript = (String)item.getProperty("itemScript");
		if (itemScript == null || itemScript.isEmpty())
		{
			// This is not necessarily an error, so it is not thrown as such
			throw new UserErrorMessage("But nothing happened.", false);
		}

		// Executes the item script
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("JavaScript");

		// Add variables to the script execution scope
		// Note: Java objects will be wrapped into JS objects automatically
		Bindings vars = new SimpleBindings();
		vars.put("request", request);

		// Run the script
		try {
			engine.eval(itemScript, vars);
		} catch (ScriptException e) {
			throw new RuntimeException("ExecuteItemScript invalid JavaScript syntax:\n -> " + e.toString());
		}

		// It is possible to get variables out of the ScriptEngine, e.g.:
		// <type> var = vars.get("variable_name");
		// Note that you can access script-defined variables this way
	}
}
