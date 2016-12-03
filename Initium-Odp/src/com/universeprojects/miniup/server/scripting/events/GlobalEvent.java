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
	public GlobalEvent(CachedEntity character, ODPDBAccess db) throws NotSupportedException {
		super(character, db);
		throw new NotSupportedException("Constructor type not supported.");
	}

	public GlobalEvent(EntityWrapper character) throws NotSupportedException {
		super(character);
		throw new NotSupportedException("Constructor type not supported.");
	}
	
	public GlobalEvent(ODPDBAccess db, Object... objects)
	{
		super(null, db);
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
