package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.List;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.dbentities.GuardSetting;

public class GuardService extends Service
{

	public GuardService(ODPDBAccess db)
	{
		super(db);
	}

	private List<GuardSetting> guardSettingsCache = null;
	private List<GuardSetting> getGuardsForLocation(CachedEntity location)
	{
		if (guardSettingsCache==null)
		{
			guardSettingsCache = new ArrayList<>();
			for(CachedEntity e:query.getFilteredList("GuardSetting", "active", true, "locationKey", location.getKey()))
			{
				guardSettingsCache.add(new GuardSetting(db, e));
			}
		}
		return guardSettingsCache;
	}
	
	
	public void canPickUp(CachedEntity character, CachedEntity territory, CachedEntity location, CachedEntity item)
	{
		
	}
	
}
