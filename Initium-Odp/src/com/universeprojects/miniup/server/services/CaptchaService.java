package com.universeprojects.miniup.server.services;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;

public class CaptchaService extends Service
{

	public CaptchaService(ODPDBAccess db)
	{
		super(db);
	}

	private CachedEntity getBotCheckEntity()
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

	public void flagBotCheckTime(CachedEntity character)
	{
		if (character==null) throw new IllegalArgumentException("Character cannot be null.");

		CachedEntity entity = character;
		
		Key userKey = (Key)character.getProperty("userKey");
		CachedEntity user = db.getEntity(userKey);
		if (user!=null) entity = user;
		
		if (entity!=null)
		{
			entity.setProperty("botCheck", true);
			db.getDB().put(entity);
			return;
		}
	}
	
	public void flagBotCheckTime()
	{
		CachedEntity entity = getBotCheckEntity();
		
		if (entity!=null)
		{
			entity.setProperty("botCheck", true);
			db.getDB().put(entity);
			return;
		}
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
	
	
}
