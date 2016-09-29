package com.universeprojects.miniup.server.services;

import java.util.Collections;
import java.util.List;

import org.mortbay.log.Log;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;

public class AntiBotService extends Service
{

	public AntiBotService(ODPDBAccess db)
	{
		super(db);
	}

	/**
	 * This fetches the possible antibot questions from the DB and picks one at random to 
	 * assign to the User. If the current user is a throwaway account, then it assigns it 
	 * to the character instead.
	 * 
	 * @param putToDB
	 */
	public void issueNewQuestion(boolean putToDB)
	{
		CachedEntity character = db.getCurrentCharacter();
		CachedEntity user = db.getCurrentUser();
		
		
		List<Key> antiBotQuestions = db.getDB().fetchAsList_Keys("AntiBotQuestion", null, 1000);
		Collections.shuffle(antiBotQuestions);
		if (antiBotQuestions.isEmpty())
		{
			Log.warn("No AntiBotQuestion entities found in the database.");
			return;
		}
		
		Key antiBotQuestionKey = antiBotQuestions.get(0);
		
		if (user!=null)
		{
			user.setProperty("antiBotQuestionKey", antiBotQuestionKey);
			if (putToDB)
				db.getDB().put(user);
		}
		else if (character!=null)
		{
			character.setProperty("antiBotQuestionKey", antiBotQuestionKey);
			if (putToDB)
				db.getDB().put(character);
			
		}
	}
	
	public CachedEntity getAntiBotQuestion()
	{
		Key abq = null;
		
		CachedEntity user = db.getCurrentUser();
		if (user!=null && user.getProperty("antiBotQuestionKey")!=null)
			abq = (Key)user.getProperty("antiBotQuestionKey");
		else
		{
			CachedEntity character = db.getCurrentCharacter();
			if (character!=null && character.getProperty("antiBotQuestionKey")!=null)
				abq = (Key)character.getProperty("antiBotQuestionKey");
		}
		
		if (abq==null) return null;
		
		return db.getEntity(abq);
	}
	
	public boolean isAntiBotQuestionActive()
	{
		CachedEntity user = db.getCurrentUser();
		if (user!=null && user.getProperty("antiBotQuestionKey")!=null)
			return true;

		CachedEntity character = db.getCurrentCharacter();
		if (character!=null && character.getProperty("antiBotQuestionKey")!=null)
			return true;
		
		return false;
	}

	public void clearAndSave()
	{
		CachedEntity user = db.getCurrentUser();
		if (user!=null && user.getProperty("antiBotQuestionKey")!=null)
		{
			user.setProperty("antiBotQuestionKey", null);
			db.getDB().put(user);
			return;
		}

		CachedEntity character = db.getCurrentCharacter();
		if (character!=null && character.getProperty("antiBotQuestionKey")!=null)
		{
			character.setProperty("antiBotQuestionKey", null);
			db.getDB().put(character);
			return;
		}
		
	}
}
