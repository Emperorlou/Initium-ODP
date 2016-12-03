package com.universeprojects.miniup.server.scripting.wrappers;

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
	}
}
