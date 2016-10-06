package com.universeprojects.miniup.server.scripting.jsaccessors;

import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.scripting.events.GlobalEvent;
import com.universeprojects.miniup.server.scripting.events.ScriptEvent;
import com.universeprojects.miniup.server.scripting.wrappers.EntityWrapper;
import com.universeprojects.miniup.server.services.ScriptService;

/**
 * Wraps ODPDBAccess class in order to limit the available DBA methods and their functionality.
 * 
 * This class is an incomplete. For the time being, you would need to add wrappers for any other
 * methods you want to use from the DBA.
 * 
 * @author aboxoffoxes
 * @author spfiredrake
 *
 */
public class DBAccessor {
	private final ODPDBAccess db;
	private final HttpServletRequest request;
	// Constitutes the set of all the kinds this wrapper is allowed to fetch
	private Set<String> allowedKinds = new HashSet<String>(Arrays.asList(
			"Item",
			"Character",
			"Location"
			));
	
	public DBAccessor(ODPDBAccess db, HttpServletRequest request)
	{
		this.db = db;
		this.request = request;
	}
	
	public DBAccessor(ODPDBAccess db, HttpServletRequest request, Set<String> allowedKinds)
	{
		this.db = db;
		this.request = request;
		this.allowedKinds = allowedKinds;
	}
	
	public EntityWrapper getCurrentCharacter()
	{
		return ScriptService.wrapEntity(db.getCurrentCharacter(), db);
	}
	
	public boolean executeScript(ScriptEvent currentEvent, String scriptName, Object... entities)
	{
		List<CachedEntity> scripts = db.getFilteredList("Script", "name", scriptName);
		if(scripts.size() > 1)
			throw new RuntimeException("Duplicate scripts detected: " + scriptName);
		if(scripts.size() == 0)
		{
			ScriptService.log.log(Level.ALL, "Script with internal name " + scriptName + " not found.");
			return false;
		}
		CachedEntity script = scripts.get(0);
		ScriptService service = ScriptService.getScriptService(db);
		GlobalEvent event = new GlobalEvent(db, entities);
		boolean executed = service.executeScript(event, script, (EntityWrapper)null);
		if(executed)
		{
			// Same thing, pass off the saving/deleting of entities to the original context. 
			for(EntityWrapper save:event.saveEntities)
				currentEvent.saveEntity(save);
			for(EntityWrapper delete:event.deleteEntities)
				currentEvent.deleteEntity(delete);
			currentEvent.haltExecution |= event.haltExecution;
			currentEvent.errorText += event.errorText;
			currentEvent.descriptionText += event.descriptionText;
		}
		return executed;
	}
}
