package com.universeprojects.miniup.server.services;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;

public class ScriptService extends Service 
{
	final static Logger log = Logger.getLogger(ScriptService.class.getName());
	CachedEntity currentCharacter;
	
	public ScriptService(ODPDBAccess db, CachedEntity character)
	{
		super(db);
		
		log.setLevel(Level.FINEST);
	}
	
	
}
