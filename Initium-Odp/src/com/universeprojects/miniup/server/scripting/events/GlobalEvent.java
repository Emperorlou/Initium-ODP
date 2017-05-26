package com.universeprojects.miniup.server.scripting.events;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.transaction.NotSupportedException;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.scripting.wrappers.EntityWrapper;
import com.universeprojects.miniup.server.services.ScriptService;

public class GlobalEvent extends ScriptEvent {
	public List<Object> arguments = new ArrayList<Object>();
	public GlobalEvent(CachedEntity character, ODPDBAccess db) {
		super(character, db);
	}

	public GlobalEvent(EntityWrapper character) {
		super(character);
	}
	
	public GlobalEvent(ODPDBAccess db, Object... objects)
	{
		super(null, db);
		addArguments(db, objects);
	}
	
	public void addArguments(ODPDBAccess db, Object... objects)
	{
		if(objects == null || objects.length == 0) return;
		
		for(Object obj:objects)
		{
			if(obj instanceof CachedEntity)
			{
				try
				{
					EntityWrapper wrapped = ScriptService.wrapEntity((CachedEntity)obj, db);
					arguments.add(wrapped);
				}
				catch(Exception ex)
				{
					ScriptService.log.log(Level.WARNING, "Unable to create script wrapper for CachedEntity. It is advisable that you do not use raw CachedEntity objects in script context.", ex);
					arguments.add(obj);
				}
			}
			else
			{
				arguments.add(obj);
			}
		}
	}

	@Override
	public String eventKind() {
		return "Global";
	}
}
