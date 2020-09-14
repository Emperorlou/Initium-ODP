package com.universeprojects.miniup.server.longoperations;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.EntityPool;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.InitiumEntityPool;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.GridMapService;
import com.universeprojects.miniup.server.services.ODPInventionService;
import com.universeprojects.miniup.server.services.ODPKnowledgeService;

public class LongOperationExperiment extends LongOperation
{

	public LongOperationExperiment(ODPDBAccess db, Map<String, String[]> requestParameters) throws UserErrorMessage
	{
		super(db, requestParameters);
	}

	
	@Override
	public String getPageRefreshJavascriptCall() {
		return "doExperiment(null);";
	}
	
	@Override
	int doBegin(Map<String, String> parameters) throws UserErrorMessage
	{
//		if(db.isTestServer()==false)
//			throw new UserErrorMessage("Experimentation is temporarily disabled.");
		
		CachedEntity character = db.getCurrentCharacter();
		doChecks(character);
		
		
		// All the items that were selected to experiment with...
		List itemKeys = null;
		String itemIdsStr = parameters.get("itemIds");
		if (itemIdsStr!=null)
		{
			itemKeys = new ArrayList<Key>();
			for(String itemIdStr:itemIdsStr.split(","))
				itemKeys.add(KeyFactory.createKey("Item", Long.parseLong(itemIdStr)));
		}


		ODPInventionService inventionService = db.getInventionService(character, null);
		
		Long selectedTileX = getSelectedTileX();
		Long selectedTileY = getSelectedTileY();
		if (GameUtils.equals(selectedTileX, 500L)) selectedTileX = null;
		if (GameUtils.equals(selectedTileY, 500L)) selectedTileY = null;
		
		
		// If no items were selected, we'll default to all items
		if (itemKeys==null || itemKeys.isEmpty())
			itemKeys = inventionService.getAvailableItemKeys(selectedTileX, selectedTileY);
		
		setDataProperty("selectedItems", itemKeys);
		setDataProperty("selectedTileX", selectedTileX);
		setDataProperty("selectedTileY", selectedTileY);
		
		
		return 5;
	}

	@Override
	String doComplete() throws UserErrorMessage
	{
		ds.beginBulkWriteMode();
		CachedEntity character = db.getCurrentCharacter();
		doChecks(character);
		
		CachedEntity location = db.getCharacterLocation(character);
		
		ODPKnowledgeService knowledgeService = db.getKnowledgeService(character.getKey());
		ODPInventionService inventionService = db.getInventionService(character, knowledgeService);
		GridMapService gms = db.getGridMapService();
		
		Long selectedTileX = (Long)getDataProperty("selectedTileX");
		Long selectedTileY = (Long)getDataProperty("selectedTileY");
		
		InitiumEntityPool pool = new InitiumEntityPool(ds, db.getGridMapService());
		String message = "";
		
		// Research one of the available items at random
		@SuppressWarnings("unchecked")
		List selectedItems = (List<Key>)getDataProperty("selectedItems");
		if (selectedItems==null)
			selectedItems = inventionService.getAvailableItemKeys(selectedTileX, selectedTileY);
		Collections.shuffle(selectedItems);
		
		for(Object k:selectedItems)
		{
			if (k instanceof Key)
				pool.addToQueue(k);
		}
		pool.loadEntities();
		
		// Gain some experience with one of the items available
		for(Object itemKey:selectedItems)
		{
			if (itemKey instanceof Key)
			{
				if (knowledgeService.increaseKnowledgeFor(pool.get(itemKey), new Random().nextInt(2)+1, 10))
				{
					message = "You've gained some experience with the "+pool.get(itemKey).getProperty("name")+".";
					break;
				}
			}
			else if (itemKey instanceof String)
			{
				CachedEntity item = gms.generateSingleItemFromProceduralKey(db, location, (String)itemKey);
				if (knowledgeService.increaseKnowledgeFor(item, new Random().nextInt(2)+1, 10))
				{
					message = "You've gained some experience with the "+item.getProperty("name")+".";
					break;
				}
			}
		}
		
		// Attempt to learn a new idea
		CachedEntity idea = inventionService.attemptToDiscoverIdea(selectedTileX, selectedTileY, selectedItems, pool);
		if (idea!=null)
		{
			message="<div class='message-newidea'>You have a new idea to "+idea.getProperty("name")+"!</div>"+message;
		}
		
		if (message.equals(""))
			message = "You couldn't find anything interesting to experiment with. Try experimenting with something different.";
		
		setUserMessage(message);
		
		ds.commitBulkWrite();
		
		return "Experimentation complete.";
	}

	private void doChecks(CachedEntity character) throws UserErrorMessage
	{
		String mode = (String)character.getProperty("mode");
		
		//NORMAL,COMBAT,MERCHANT,TRADING,UNCONSCIOUS,DEAD
		if ("TRADING".equals(mode))
			throw new UserErrorMessage("You cannot experiment right now. You're currently trading.");
		else if ("COMBAT".equals(mode))
			throw new UserErrorMessage("You cannot experiment right now. You're currently in combat.");
		else if ("MERCHANT".equals(mode))
			throw new UserErrorMessage("You cannot experiment right now. You're currently maning your store. <br><a onclick='closeAllPopups(); toggleStorefront();'>Shutdown Store</a>");
		else if ("UNCONSCIOUS".equals(mode))
			throw new UserErrorMessage("You cannot experiment right now. You're currently unconscious, lol.");
		else if ("DEAD".equals(mode))
			throw new UserErrorMessage("You cannot experiment right now. You're DEAD. D:");
		else if ("NORMAL".equals(character.getProperty("mode"))==false)
			throw new UserErrorMessage("You cannot experiment right now. You're too busy.");		
	}
	

}
