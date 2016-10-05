package com.universeprojects.miniup.server.scripting.events;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.scripting.wrappers.EntityWrapper;

/**
 * Represents a simple scripting event (such as clicking a link). Typically no additional
 * data needs to be passed to the script context, as the item generating the script
 * will be passed in the executeScript call already.
 *   
 * @author spfiredrake
 */
public class SimpleEvent extends ScriptEvent 
{
	public SimpleEvent(CachedEntity character, ODPDBAccess db)
	{
		super(character, db);
	}
	
	public SimpleEvent(EntityWrapper character) 
	{
		super(character);
	}

	@Override
	public String eventKind() {
		return "Simple";
	}

}
