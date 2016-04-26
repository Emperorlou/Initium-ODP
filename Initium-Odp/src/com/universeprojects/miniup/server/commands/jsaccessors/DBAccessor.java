package com.universeprojects.miniup.server.commands.jsaccessors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;

/**
 * Wraps ODPDBAccess class in order to limit the available DBA methods and their functionality.
 * Most notably, it contains a whitelist of the kinds this wrapper is allowed to access.
 * 
 * This class is an incomplete. For the time being, you would need to add wrappers for any other
 * methods you want to use from the DBA.
 * 
 * @author aboxoffoxes
 *
 */
public class DBAccessor {
	private final ODPDBAccess db;
	private final HttpServletRequest request;
	// Constitutes the set of all the kinds this wrapper is allowed to fetch
	private Set<String> allowedKinds = new HashSet<String>(Arrays.asList(
			"item",
			"character"
			));
	
	public DBAccessor(ODPDBAccess db, HttpServletRequest request)
	{
		this.db = db;
		this.request = request;
	}
	
	public DBAccessor(ODPDBAccess db, HttpServletRequest request, Set<String> allowedKinds)
	{
		this.db = db;
		this.request = request;
		this.allowedKinds = allowedKinds;
	}
	
	public CachedEntity getCurrentUser()
	{
		return db.getCurrentUser(request);
	}
	
	public CachedEntity getCurrentCharacter()
	{
		return db.getCurrentCharacter(request);
	}
}
