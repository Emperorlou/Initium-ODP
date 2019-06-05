package com.universeprojects.miniup.server.longoperations;

import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.MainPageUpdateService;

public class LongOperationCampDefend extends LongOperation 
{

	public LongOperationCampDefend(ODPDBAccess db,
			Map<String, String[]> requestParameters) throws UserErrorMessage 
	{
		super(db, requestParameters);
	}

	@Override
	int doBegin(Map<String, String> parameters) throws UserErrorMessage,
			UserRequestIncompleteException 
	{
		CachedEntity character = db.getCurrentCharacter();
		CachedEntity location = db.getEntity((Key)character.getProperty("locationKey"));
		
		if (GameUtils.isPlayerIncapacitated(character))
			throw new UserErrorMessage("You're incapacitated, you can't do this right now.");
		if (GameUtils.isContainedInList((String)location.getProperty("type"), "CampSite")==false)
			throw new UserErrorMessage("You are not located in an area that requires defending.");
		
		if(GameUtils.isCharacterInParty(character) && GameUtils.isCharacterPartyLeader(character)==false)
			throw new UserErrorMessage("You are not the party leader and cannot defend camps.");
		
		Double monsterCount = db.getMonsterCountForLocation(ds, location);
		if(monsterCount == null || monsterCount < 1)
			throw new UserErrorMessage("There is no monster activity threatening this camp. You can rest easy.");
		
		setLongOperationName("Defending Camp");
		setLongOperationDescription("You are patroling the camp in search of nearby attackers.");
		
		return 1;
	}

	@Override
	String doComplete() throws UserErrorMessage, UserRequestIncompleteException 
	{
		CachedEntity character = db.getCurrentCharacter();
		CachedEntity location = db.getEntity((Key)character.getProperty("locationKey"));
		
		ds.beginBulkWriteMode();
		boolean foundMonster = db.randomMonsterEncounter(ds, character, location, 5, 1d);
		ds.commitBulkWrite();
		
		String description = "You stood guard but found nothing.";
		if(foundMonster)
		{
			location = db.getEntity((Key)character.getProperty("locationKey"));
			description = "You found something attacking the camp!";
		}
		
		MainPageUpdateService mpus = MainPageUpdateService.getInstance(db, db.getCurrentUser(), character, location, this);
		if(foundMonster)
			mpus.updateFullPage_shortcut();
		else
		{
			mpus.updateInBannerOverlayLinks();
			mpus.updateMonsterCountPanel();
		}
		
		return description;
	}

	@Override
	public String getPageRefreshJavascriptCall() 
	{
		return "doCampDefend()";
	}

}
