package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.scripting.events.ScriptEvent;
import com.universeprojects.miniup.server.scripting.jsaccessors.DBAccessor;
import com.universeprojects.miniup.server.scripting.wrappers.Buff;
import com.universeprojects.miniup.server.scripting.wrappers.Item;
import com.universeprojects.miniup.server.scripting.wrappers.Character;
import com.universeprojects.miniup.server.scripting.wrappers.Location;
import com.universeprojects.miniup.server.scripting.wrappers.EntityWrapper;

public class ScriptService extends Service 
{
	public final static Logger log = Logger.getLogger(ScriptService.class.getName());
	private Context jsContext = null;
	private Scriptable jsScope = null;
	private boolean canExecute = false;
	
	public static ScriptService getScriptService(ODPDBAccess db)
	{
		HttpServletRequest request = db.getRequest();
		if (request.getAttribute("scriptService") != null) return (ScriptService) request.getAttribute("scriptService");

		ScriptService service = new ScriptService(db, request);
		request.setAttribute("scriptService", service);

		return service;
	}
	
	private ScriptService(ODPDBAccess db, HttpServletRequest request)
	{
		super(db);
		log.setLevel(Level.ALL);
		
		try
		{
			jsContext = Context.enter();
			// This sandboxes the engine by preventing access to non-standard objects and some reflexion objects
	    	// This is the simplest way to prevent access to top-level packages
			jsScope = jsContext.initSafeStandardObjects();
			DBAccessor coreFunctions = new DBAccessor(db, request);
			jsScope.put("core", jsScope, coreFunctions);
			canExecute = true;
		}
		catch(Exception ex)
		{
			log.log(Level.WARNING, "Failed to initialize ScriptService", ex);
		}
	}
	
	/**
	 * Static method to wrap a CachedEntity in the EntityWrapper, for use in the script context.
	 * @param entity CachedEntity object
	 * @param db DB instance, needed for entity specific functions (moving items, getting buffs, etc).
	 * @return
	 */
	public static EntityWrapper wrapEntity(CachedEntity entity, ODPDBAccess db)
	{
		// We want to allow null wrappers to be returned, in cases such as GlobalEvent,
		// where we pass a null CachedEntity for the currentCharacter parameter.
		if(entity == null) return null;
		switch(entity.getKind())
		{
			case "Item":
				return new Item(entity, db);
			case "Character":
				return new Character(entity, db);
			case "Location":
				return new Location(entity, db);
			case "Buff":
				return new Buff(entity, db);
			default: 
				throw new RuntimeException("Entity does not support scripting.");
		}
	}
	
	/**
	 * Executes the script. Wraps entitySource in the script wrapper type first.  
	 * @param scriptEntity The actual script entity itself.
	 * @param entitySource The CachedEntity which fired the script.
	 * @return True if script executed successfully, false otherwise.
	 */
	public boolean executeScript(ScriptEvent event, CachedEntity scriptEntity, CachedEntity entitySource)
	{
		return executeScript(event, scriptEntity, ScriptService.wrapEntity(entitySource, this.db));
	}
	
	/**
	 * Including ODPDBAccess parameter here, due to issues with type erasure
	 */
	public boolean executeScript(ScriptEvent event, CachedEntity scriptEntity, ODPDBAccess db, List<CachedEntity> entitySources)
	{
		List<EntityWrapper> wrappers = new ArrayList<EntityWrapper>();
		for(CachedEntity entity:entitySources)
			wrappers.add(ScriptService.wrapEntity(entity, db));
		return executeScript(event, scriptEntity, wrappers);
	}
	
	public boolean executeScript(ScriptEvent event, CachedEntity scriptEntity, EntityWrapper entitySource)
	{
		return executeScript(event, scriptEntity, Arrays.asList(entitySource));
	}
	
	/**
	 * Executes the script. Wraps entitySource in the script wrapper type first.  
	 * @param scriptEntity The actual script entity itself.
	 * @param entitySource The entity which fired the script, will be passed into the script context to utilize in source.
	 * @return True if script executed successfully, false otherwise.
	 */
	public boolean executeScript(ScriptEvent event, CachedEntity scriptEntity, List<EntityWrapper> entitySources)
	{
		if(event == null) throw new IllegalArgumentException("event cannot be null!");
		
		if(!canExecute)
		{
			log.log(Level.ALL, "Script service is not properly initialized!");
			return false;
		}
		
		// Does this script actually do anything?
		String script = (String)scriptEntity.getProperty("script");
		if (script == null || script.isEmpty())
		{
			// This is not necessarily an error, so it is not thrown as such
			event.errorText = "But nothing happened.";
			return false;
		}
		
		String scriptName = (String)scriptEntity.getProperty("internalName");
	    try
	    {
	    	log.log(Level.WARNING, "Executing script: " + scriptEntity.getKey().getId());
	    	// Create a new scope for every script execution call.
	    	Scriptable currentScope = jsContext.newObject(jsScope);
	    	currentScope.setPrototype(jsScope);
	    	currentScope.setParentScope(null);
			// Put the event into scope, so we can use it
	    	currentScope.put("event", currentScope, Context.toObject(event, currentScope));
	    	// Recreate the sourceEntity variable on each hit.
	    	for(EntityWrapper src:entitySources)
	    	{
	    		// Always delete the sourceEntity from scope.
	    		if(currentScope.has("sourceEntity", currentScope)) 
	    			currentScope.delete("sourceEntity");
	    		
	    		// Only put it back if it's not null.
		    	if(src != null)
		    		currentScope.put("sourceEntity", currentScope, Context.toObject(src, currentScope));
		    	
		    	// Evaluate the script. We don't need to return anything, everything we need
		    	// is on the Event object itself.
		    	jsContext.evaluateString(currentScope, script, scriptName, 0, null);
	    	}
	    	currentScope = null;
	    	return true;
	    }
	    catch (Exception e)
	    {
	    	log.log(Level.SEVERE, "Exception during Script exection of " + scriptName, e);
	    }
		return false;
	}
	
	/**
	 * Exits the script context and nulls out the references. Should be called when the 
	 * ScriptService is no longer needed, but just in case it isn't will be handled by
	 * the finalizer (when eventually garbage collected).
	 */
	public void close()
	{
		if(jsContext != null)
		{
			jsScope = null;
			jsContext = null;
			Context.exit();
		}
	}
	/**
	 * We need to ensure script context is cleared. Finalizer is called when garbage collected,
	 * and since we reference Native resources, we need to make sure we exit the context cleanly
	 * in case the reference is not disposed of properly.
	 */
	protected void finalize( ) throws Throwable
	{
		close();
	}
}
