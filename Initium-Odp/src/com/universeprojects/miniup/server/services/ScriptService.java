package com.universeprojects.miniup.server.services;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.scripting.events.ScriptEvent;
import com.universeprojects.miniup.server.scripting.jsaccessors.CommandAccessor;
import com.universeprojects.miniup.server.scripting.jsaccessors.DBAccessor;
import com.universeprojects.miniup.server.scripting.wrappers.Item;
import com.universeprojects.miniup.server.scripting.wrappers.Character;
import com.universeprojects.miniup.server.scripting.wrappers.Location;
import com.universeprojects.miniup.server.scripting.wrappers.EntityWrapper;

public class ScriptService extends Service 
{
	final static Logger log = Logger.getLogger(ScriptService.class.getName());
	private ScriptEvent firingEvent;
	private Context jsContext = null;
	private Scriptable jsScope = null;
	private boolean canExecute = false;
	
	public ScriptService(ODPDBAccess db, ScriptEvent event)
	{
		super(db);
		log.setLevel(Level.FINEST);
		
		firingEvent = event;
		try
		{
			jsContext = Context.enter();
			// This sandboxes the engine by preventing access to non-standard objects and some reflexion objects
	    	// This is the simplest way to prevent access to top-level packages
			jsScope = jsContext.initSafeStandardObjects();
			// Put the event into scope, so we can use it
			jsScope.put("event", jsScope, Context.toObject(firingEvent, jsScope));
		}
		catch(Exception ex)
		{
			log.log(Level.ALL, "Failed to initialize ScriptService", ex);
			canExecute = false;
		}
	}
	
	public static EntityWrapper wrapEntity(CachedEntity entity, ODPDBAccess db)
	{
		switch(entity.getKind())
		{
			case "Item":
				return new Item(entity, db);
			case "Character":
				return new Character(entity, db);
			case "Location":
				return new Location(entity, db);
			default: 
				throw new RuntimeException("Entity does not support scripting.");
		}
	}
	
	/**
	 * 
	 * @param scriptEntity
	 * @param entitySource
	 * @return
	 */
	public boolean executeScript(CachedEntity scriptEntity, CachedEntity entitySource)
	{
		return executeScript(scriptEntity, ScriptService.wrapEntity(entitySource, this.db));
	}
	
	public boolean executeScript(CachedEntity scriptEntity, EntityWrapper entitySource)
	{
		// Does this script actually do anything?
		String script = (String)scriptEntity.getProperty("script");
		if (script == null || script.isEmpty())
		{
			// This is not necessarily an error, so it is not thrown as such
			firingEvent.errorText = "But nothing happened.";
			return false;
		}
		
	    try
	    {
	    	// Recreate the sourceEntity variable on each hit.
	    	if(jsScope.has("sourceEntity", jsScope))
	    		jsScope.delete("sourceEntity");
	    	jsScope.put("sourceEntity", jsScope, Context.toObject(firingEvent, jsScope));
	    	// Evaluate the script. We don't need to return anything, everything we need
	    	// is on the Event object itself.
	    	jsContext.evaluateString(jsScope, script, "scriptName", 0, null);
	    	return true;
	    }
	    catch (Exception e)
	    {
	    	log.log(Level.ALL, "Exception during Script exection!", e);
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
