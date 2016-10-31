package com.universeprojects.miniup.server.services;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;

public class CaptchaService extends Service
{

	public CaptchaService(ODPDBAccess db)
	{
		super(db);
	}

	protected CachedEntity getBotCheckEntity()
	{
		CachedEntity entity = db.getCurrentUser();
		if (entity==null)
			entity = db.getCurrentCharacter();
		return entity;
	}

	
	public boolean isBotCheckTime()
	{
		CachedEntity entity = getBotCheckEntity();

		if (entity!=null)
			return Boolean.TRUE.equals(entity.getProperty("botCheck"));
		
		return false;
	}


	
	public void flagCheckSucceeded()
	{
		CachedEntity entity = getBotCheckEntity();
		if (entity!=null)
		{
			entity.setProperty("botCheck", false);
			db.getDB().put(entity);
			return;
		}
	}

	public void flagActionAttempted()
	{
		// Do nothing
	}
	
	
}
