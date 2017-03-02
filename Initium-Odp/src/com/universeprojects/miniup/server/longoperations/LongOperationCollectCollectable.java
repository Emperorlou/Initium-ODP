package com.universeprojects.miniup.server.longoperations;

import java.util.Map;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class LongOperationCollectCollectable extends LongOperation {

	public LongOperationCollectCollectable(ODPDBAccess db,
			Map<String, String[]> requestParameters)
			throws UserErrorMessage {
		super(db, requestParameters);
		// TODO Auto-generated constructor stub
	}

	@Override
	int doBegin(Map<String, String> parameters) throws UserErrorMessage {
		
		if (ODPDBAccess.CHARACTER_MODE_COMBAT.equals(db.getCurrentCharacter().getProperty("mode")))
			throw new UserErrorMessage("You cannot collect stuff right now because you are currently in combat.");
		if (ODPDBAccess.CHARACTER_MODE_MERCHANT.equals(db.getCurrentCharacter().getProperty("mode")))
			throw new UserErrorMessage("You cannot collect stuff right now because you are currently vending.");
		if (ODPDBAccess.CHARACTER_MODE_TRADING.equals(db.getCurrentCharacter().getProperty("mode")))
			throw new UserErrorMessage("You cannot collect stuff right now because you are currently trading.");
		if (db.getCurrentCharacter().getProperty("mode")==null || "".equals(db.getCurrentCharacter().getProperty("mode")) || ODPDBAccess.CHARACTER_MODE_NORMAL.equals(db.getCurrentCharacter().getProperty("mode")))
		{/*We're in normal mode and so we can actually move*/}
		else
			throw new UserErrorMessage("You cannot collect stuff right now because you are busy.");
		
		
		
		String collectableIdStr = parameters.get("collectableId");
		CachedEntity collectable = db.getEntity("Collectable", Long.parseLong(collectableIdStr));
		
		if (GameUtils.equals(collectable.getProperty("locationKey"), db.getCurrentCharacter().getProperty("locationKey"))==false)
			throw new UserErrorMessage("You cannot collect this, you're not even near it!");
		
		if ((Long)collectable.getProperty("collectionCount")<=0)
		{
			ds.delete(collectable);
			throw new UserErrorMessage("Looks like there's nothing left to collect here.");
		}
		
		int seconds = ((Long)collectable.getProperty("extractionEase")).intValue();
		
		data.put("collectableId", collectable.getKey().getId());
		data.put("secondsToWait", seconds);
		data.put("bannerUrl", collectable.getProperty("bannerUrl"));
		
		return seconds;
	}

	@Override
	String doComplete() throws UserErrorMessage {
		Long collectableId = (Long)data.get("collectableId");
		CachedEntity collectable = db.getEntity("Collectable", collectableId);
		
		// Now update the collectable..
		Long collectionCount = (Long)collectable.getProperty("collectionCount");
		collectionCount--;
		if (collectionCount<=0)
		{
			// Delete the collectable 
			ds.delete(collectable);
		}
		else
		{
			collectable.setProperty("collectionCount", collectionCount);
			
			ds.put(collectable);
		}
		
		
		CachedEntity itemDef = db.getEntity((Key)collectable.getProperty("itemDefKey"));
		
		if (itemDef==null)
			throw new RuntimeException("ItemDef is null on the collectable.");
		
		CachedEntity item = db.generateNewObject(itemDef, "Item");
		
		// Finish off some of those properties..
		item.setProperty("containerKey", db.getCurrentCharacter().getKey());
		ds.put(item);
		
		return "You collected: "+GameUtils.renderItem(item);
	}

	@Override
	public String getPageRefreshJavascriptCall() {
		Long collectableId = (Long)data.get("collectableId");
		return "doCollectCollectable(null, "+collectableId+");";
	}

	@Override
	public Map<String, Object> getStateData() {
		Map<String, Object> stateData = super.getStateData();
		
		stateData.put("secondsToWait", data.get("secondsToWait"));
		
		return stateData;
	}

	
	
}
