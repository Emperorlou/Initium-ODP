package com.universeprojects.miniup.server.services;

import com.universeprojects.miniup.server.ODPDBAccess;

public abstract class Service
{
	final ODPDBAccess db;
	
	public Service(ODPDBAccess db)
	{
		this.db = db;
	}
}
