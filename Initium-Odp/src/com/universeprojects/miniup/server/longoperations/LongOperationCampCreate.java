package com.universeprojects.miniup.server.longoperations;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class LongOperationCampCreate extends LongOperation 
{

	public LongOperationCampCreate(ODPDBAccess db,
			Map<String, String[]> requestParameters) throws UserErrorMessage 
	{
		super(db, requestParameters);
	}

	@Override
	int doBegin(Map<String, String> parameters) throws UserErrorMessage,
			UserRequestIncompleteException 
	{
		String campName = parameters.get("name");
		if (campName==null || campName.length()>40)
			throw new UserErrorMessage("Camp name must be between 1 and 40 characters long.");
		
		if (campName.matches(ODPDBAccess.CAMP_NAME_REGEX)==false)
			throw new UserErrorMessage("Camp name must contain only letters or numbers or any of the following characters: -,'&");
		
		CachedEntity character = db.getCurrentCharacter();
		CachedEntity location = db.getEntity((Key)character.getProperty("locationKey"));
		if (location == null || location.getProperty("supportsCamps")==null || (Long)location.getProperty("supportsCamps")==0)
			throw new UserErrorMessage("You cannot create a camp here.");
		
		if(GameUtils.isCharacterInParty(character) && GameUtils.isCharacterPartyLeader(character)==false)
			throw new UserErrorMessage("You are not the party leader and cannot create camps.");
		
		List<CachedEntity> existingCampsites = db.getCampsitesByParentLocation(ds, location.getKey());
		long locationMaxCampsites = 0;
		
		if (location.getProperty("supportsCamps")!=null)
			locationMaxCampsites = (Long)location.getProperty("supportsCamps");

		if (existingCampsites.size()>=locationMaxCampsites)
			throw new UserErrorMessage("There is already "+locationMaxCampsites+" camps in this location. You cannot create another one.");
		
		setDataProperty("name", parameters.get("name"));
		
		return 2;
	}

	@Override
	String doComplete() throws UserErrorMessage, UserRequestIncompleteException 
	{
		String campName = (String)getDataProperty("name");
		CachedEntity character = db.getCurrentCharacter();
		CachedEntity location = db.getEntity((Key)character.getProperty("locationKey"));
		location = doAttemptCreateCampsite(db, character, location, campName);
		
		String description = "While you were working on your camp you were interrupted by an attacker!"; //Building a camp takes quite some time and the fire often attracts monsters in the area. You might want to try bring the activity in the area down before building a camp. Though, depending on how much activity there is here, it could require a group effort.
		// Null location indicates monster was encountered.
		if(location != null)
			description = "Your camp has been created. Make sure to defend it and perhaps get others to help!";
		
		throw new GameStateChangeException(description);
	}

	@Override
	public String getPageRefreshJavascriptCall() 
	{
		String campName = (String)getDataProperty("campName");
		return "doCampCreate(" + campName + ")";
	}

	public static CachedEntity doAttemptCreateCampsite(ODPDBAccess db, CachedEntity character, CachedEntity parentLocation, String campName) throws UserErrorMessage
	{
		CachedDatastoreService ds = db.getDB();

		ds.beginBulkWriteMode();
		try
		{
			boolean foundMonster = db.randomMonsterEncounter(ds, character, parentLocation, 6, 1d);
			if (foundMonster)
				return null;
	
			ds.allocateIds("Location", 1);
			CachedEntity campsite = new CachedEntity("Location", ds.getPreallocatedIdFor("Location"));
			campsite.setProperty("banner", "images/banner---campsite1.jpg");
			campsite.setProperty("name", "Camp: "+campName);
			campsite.setProperty("description", "This is a player created camp.<br><br>" +
				"Defend the camp by pressing the Defend button " +
				"regularly. You can keep track of the integrity of the camp with the status line " +
				"shown below. A camp that is not adequately defended can be overrun and players will no longer be able to use it to rest.<br><br>" +
				"The speed at which a camp is overrun depends on the monster activity in the " +
				"location the camp was created in. Reducing the monster activity outside of the camp " +
				"will make the camp much easier to defend.");
			campsite.setProperty("discoverAnythingChance", 100d);
			campsite.setProperty("createdDate", new Date());
			campsite.setProperty("type", "CampSite");
			campsite.setProperty("decayRate", 100l);
			campsite.setProperty("parentLocationKey", parentLocation.getKey());
			campsite.setProperty("isOutside", "TRUE");
			campsite.setProperty("supportsCampfires", 1L);
			campsite.setProperty("maxMonsterCount", parentLocation.getProperty("maxMonsterCount"));
			campsite.setProperty("monsterRegenerationRate", parentLocation.getProperty("monsterRegenerationRate"));
			
			CachedEntity path = db.newPath(ds, "Path to camp - "+campName, parentLocation.getKey(), null, campsite.getKey(), "Leave camp", 100, 0l, "CampSite");
			ds.put(campsite, path);
			// Returns the destination.
			return db.doCharacterTakePath(ds, character, path, campsite, false, false);
		}
		finally
		{
			ds.commitBulkWrite();
		}
	}
}
