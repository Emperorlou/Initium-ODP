package com.universeprojects.miniup.server.scripting.events;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.scripting.wrappers.Character;
import com.universeprojects.miniup.server.scripting.wrappers.Location;
import com.universeprojects.miniup.server.scripting.wrappers.EntityWrapper;

public class MoveEvent extends ScriptEvent 
{
	public Location fromLocation; 
	public Location toLocation;
	public Long delay = 0L;
	
	public MoveEvent(ODPDBAccess db, CachedEntity character, CachedEntity startLocation, CachedEntity endLocation)
	{
		this(new Character(character, db), new Location(startLocation, db), new Location(endLocation, db));
	}
	
	public MoveEvent(EntityWrapper character, EntityWrapper startLocation, EntityWrapper endLocation) {
		this((Character)character, (Location)startLocation, (Location)endLocation); 
	}
	
	public MoveEvent(Character character, Location startLocation, Location endLocation)
	{
		super(character);
		fromLocation = startLocation;
		toLocation = endLocation;
	}

	public MoveEvent(CachedEntity character, ODPDBAccess db) throws UnsupportedOperationException
	{
		super(character, db);
		throw new UnsupportedOperationException("Constructor type not supported.");
	}
	
	public MoveEvent(EntityWrapper character) throws UnsupportedOperationException
	{
		super(character);
		throw new UnsupportedOperationException("Constructor type not supported.");
	}

	@Override
	public String eventKind() 
	{
		return "Move";
	}

}
