package com.universeprojects.miniup.server.scripting.wrappers;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;

/**
 * Scripting engine wrapper for the Location CachedEntity.
 * getName already exists as part of the base EntityWrapper class.
 * Only information that we really want to expose at the moment.
 * May want to find associated paths.
 * 
 * @author spfiredrake
 */
public class Location extends EntityWrapper {

	public Location(CachedEntity entity, ODPDBAccess db) {
		super(entity, db);
		isInstanceLocation = "TRUE".equals(entity.getProperty("instanceModeEnabled"));
	}
	
	/**
	 * Boolean indicating whether this location is instanced or not. Does not indicate
	 * that an instance timer is set.
	 */
	 public final boolean isInstanceLocation;
	
	/**
	 * Returns back whether an instance timer is set on the location.
	 * @return
	 */
	public boolean instanceTimerSet()
	{
		return isInstanceLocation && this.getProperty("instanceRespawnDate")!=null;
	}
	
	/**
	 * Resets the instance timer location. Only returns back whether the instance timer
	 * is running, not necessarily whether the instance timer was set.
	 * @return
	 */
	public boolean resetInstanceTimer()
	{
		db.resetInstanceRespawnTimer(this.wrappedEntity);
		return instanceTimerSet();
	}
}
