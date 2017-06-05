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
	public boolean isInstanceTimerSet()
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
		if(isInstanceLocation == false) return false;
		db.resetInstanceRespawnTimer(this.wrappedEntity);
		return isInstanceTimerSet();
	}
	
	/**
	 * Sets the instance timer at the location.
	 * @param respawnTime
	 * @return
	 */
	public boolean setInstanceTimer(Long respawnTime)
	{
		return setInstanceTimer(respawnTime, false);
	}
	
	public boolean setInstanceTimer(Long respawnTime, boolean ignoreIfSet)
	{
		if(isInstanceLocation == false) return false;
		if(ignoreIfSet && isInstanceTimerSet()) return false;
		
		GregorianCalendar cal = new GregorianCalendar();
		cal.add(Calendar.MINUTE, respawnTime.intValue());
		
		this.setProperty("instanceRespawnDate", cal.getTime());
		
		return isInstanceTimerSet();
	}
}
