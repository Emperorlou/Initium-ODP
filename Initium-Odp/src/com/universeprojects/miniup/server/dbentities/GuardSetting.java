package com.universeprojects.miniup.server.dbentities;

import java.util.HashSet;
import java.util.Set;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;

public class GuardSetting extends InitiumEntityBase
{
	public enum GuardExclusion
	{
		Group, Party
	}
	
	public enum GuardType
	{
		NoMoving,
		NoUsing,
		NoTresspassing
	}
	
	public GuardSetting(ODPDBAccess db, CachedEntity entity)
	{
		super(db, entity);
	}

	@Override
	protected String getKind()
	{
		return "GuardSetting";
	}

	public Key getCharacterKey()
	{
		return (Key)entity.getProperty("characterKey");
	}

	public void setCharacterKey(Key characterKey)
	{
		entity.setProperty("characterKey", characterKey);
	}

	public Key getEntityKey()
	{
		return (Key)entity.getProperty("entityKey");
	}

	public void setEntityKey(Key entityKey)
	{
		entity.setProperty("entityKey", entityKey);
	}

	public Set<GuardExclusion> getExclude()
	{
		String raw = (String)entity.getProperty("exclude");
		if (raw==null) return new HashSet<>();
		String[] rawValues = raw.split(",");
		
		Set<GuardExclusion> result = new HashSet<>();
		for(String val:rawValues)
			result.add(GuardExclusion.valueOf(val));
		
		return result;
	}

	public void setExclude(GuardExclusion[] exclude)
	{
		entity.setProperty("exclude", exclude);
	}

	public Key getLocationKey()
	{
		return (Key)entity.getProperty("locationKey");
	}

	public void setLocationKey(Key locationKey)
	{
		entity.setProperty("locationKey", locationKey);
	}

	public Set<GuardType> getSettings()
	{
		String raw = (String)entity.getProperty("settings");
		if (raw==null) return new HashSet<>();
		String[] rawValues = raw.split(",");
		
		Set<GuardType> result = new HashSet<>();
		for(String val:rawValues)
			result.add(GuardType.valueOf(val));
		
		return result;
	}

	public void setSettings(GuardType[] settings)
	{
		entity.setProperty("settings", settings);
	}
	
	
	public Long getLine()
	{
		return (Long)entity.getProperty("line");
	}
	
	public void setLine(Long line)
	{
		entity.setProperty("line", line);
	}
}
