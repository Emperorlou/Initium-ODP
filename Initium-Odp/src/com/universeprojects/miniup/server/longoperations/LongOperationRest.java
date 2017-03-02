package com.universeprojects.miniup.server.longoperations;

import java.util.Map;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class LongOperationRest extends LongOperation {

	public LongOperationRest(ODPDBAccess db, 
			Map<String, String[]> requestParameters) throws UserErrorMessage {
		super(db, requestParameters);
		// TODO Auto-generated constructor stub
	}

	@Override
	int doBegin(Map<String, String> parameters) throws UserErrorMessage 
	{
		Key locationKey = (Key)db.getCurrentCharacter().getProperty("locationKey");
		CachedEntity location = db.getEntity(locationKey);
		
		String locationType = (String)location.getProperty("type");
		if (GameUtils.isPlayerIncapacitated(db.getCurrentCharacter()))
			throw new UserErrorMessage("You're incapacitated, you can't do this right now.");
		if ("RestSite".equals(locationType)==false && "CampSite".equals(locationType)==false)
			throw new UserErrorMessage("You cannot rest here. Find a rest site like a camp or an Inn, or even a player's house.");
		
		Double hitpointsToRegain = (Double)db.getCurrentCharacter().getProperty("maxHitpoints")-(Double)db.getCurrentCharacter().getProperty("hitpoints");
		if (hitpointsToRegain<=0)
			throw new UserErrorMessage("You don't need to rest, you're already at full health! NOW GET OUT THERE AND KICK SOME ASS!");
		
		data.put("description", "It will take "+hitpointsToRegain.intValue()+" seconds to regain your health.");
		
		return hitpointsToRegain.intValue();
	}

	@Override
	String doComplete() throws UserErrorMessage {
		db.doCharacterRestFully(db.getCurrentCharacter());
		
		Key locationKey = (Key)db.getCurrentCharacter().getProperty("locationKey");
		CachedEntity location = db.getEntity(locationKey);
		
		// If the character is resting in a "nice" location, then give the well rested buff
		// ie. If this is a player house, then it should be a RestSite and have an owner.
		if ("RestSite".equals(location.getProperty("type")) &&
				location.getProperty("ownerKey")!=null)
		{
			db.awardBuff_WellRested(ds, db.getCurrentCharacter());
		}
		
		return "You are fully rested!";
	}


	@Override
	public String getPageRefreshJavascriptCall() {
		return "doRest()";
	}

	@Override
	public Map<String, Object> getStateData() {
		Map<String, Object> stateData = super.getStateData();
		
		stateData.put("description", data.get("description"));
		
		return stateData;
	}

	
	
}
