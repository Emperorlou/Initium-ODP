package com.universeprojects.miniup.server.commands;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

/**
 * Makes it possible to execute script associated with an item.
 * 
 * Keep in mind that this command is meant to be safe: if you add more variables to the script scope,
 * make sure to keep it secret, keep it safe.
 * 
 * @author aboxoffoxes
 *
 */
public class CommandExecuteScript extends Command {

	public CommandExecuteScript(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		ODPDBAccess db = getDB();

		// Safely fetch script and item entities
		Long sourceEntityId = tryParseId(parameters, "sourceEntityId");
		String sourceEntityKind = parameters.get("sourceEntityKind");
		CachedEntity sourceEntity = db.getEntity(sourceEntityKind, sourceEntityId);

		Long scriptId = tryParseId(parameters, "scriptId");
		CachedEntity scriptEntity = db.getEntity("script", scriptId);

		// Is this source of the right kind?
		Set<String> allowedKinds = new HashSet<String> (Arrays.asList("item", "character"));
		if (allowedKinds.contains(sourceEntity.getKind())==false)
		{
			throw new UserErrorMessage("Entity is not allowed to execute scripts this way.");
		}

		// Does this source have this script?
		Map<String, Object> sourceProperties = sourceEntity.getProperties();
		@SuppressWarnings("unchecked")
		Set<Long> sourceScripts = (Set<Long>)sourceProperties.get("scripts");
		if (sourceScripts.contains(scriptId)==false)
		{
			throw new UserErrorMessage("The entity does not have this effect!");
		}

		// Can player trigger this effect...
		CachedEntity playerEntity = db.getCurrentCharacter(request);

		switch(sourceEntity.getKind())
		{
			// ...by being its originator:
			case("character"):
			{
				if (playerEntity.equals(sourceEntityId)==false)
				{
					throw new UserErrorMessage("You are not allowed to trigger others");
				}
			}
			case("item"):
			{
				// ...by being close enough to the item OR having it in their pocket
				Key containerKey = (Key)sourceEntity.getProperty("containerKey");
				if (containerKey==null)
				{
					throw new UserErrorMessage("You can only trigger items in your vicinity!");
				}
				if (GameUtils.equals(containerKey,playerEntity.getKey())==false
					&& GameUtils.equals(containerKey,(Key)playerEntity.getProperty("locationKey"))==false)
				{
					throw new UserErrorMessage("You can only trigger items in your vicinity!");
				}
				break;
			}
			default:
			{
				throw new RuntimeException("CommandExecuteItemScript: unhandled source entity type");
			}
		}


		// Does this script actually do anything?
		String script = (String)scriptEntity.getProperty("script");
		if (script == null || script.isEmpty())
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
			engine.eval(script, vars);
		} catch (ScriptException e) {
			throw new RuntimeException("ExecuteItemScript invalid JavaScript syntax:\n -> " + e.toString());
		}

		// It is possible to get variables out of the ScriptEngine, e.g.:
		// <type> var = vars.get("variable_name");
		// Note that you can access script-defined variables this way
	}
}
