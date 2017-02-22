package com.universeprojects.miniup.server.longoperations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.EntityPool;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.ODPInventionService;
import com.universeprojects.miniup.server.services.ODPKnowledgeService;

public class LongOperationCreatePrototype extends LongOperation
{

	public LongOperationCreatePrototype(ODPDBAccess db, Map<String, String[]> requestParameters) throws UserErrorMessage
	{
		super(db, requestParameters);
	}

	
	@Override
	public String getPageRefreshJavascriptCall() {
		return "doCreatePrototype(null, '"+data.get("ideaName")+"');";
	}
	
	@Override
	int doBegin(Map<String, String> parameters) throws UserErrorMessage
	{
		CachedEntity character = db.getCurrentCharacter();
		doChecks(character);
		
		
		// All the items that were selected to experiment with...
		List<Key> itemKeys = null;
		String itemIdsStr = parameters.get("itemIds");
		if (itemIdsStr!=null)
		{
			itemKeys = new ArrayList<Key>();
			for(String itemIdStr:itemIdsStr.split(","))
				itemKeys.add(KeyFactory.createKey("Item", Long.parseLong(itemIdStr)));
		}


		ODPInventionService inventionService = db.getInventionService(character, null);
		
		// If no items were selected, we'll default to all items
		if (itemKeys==null || itemKeys.isEmpty())
			itemKeys = inventionService.getAvailableItemKeys();
		
		data.put("selectedItems", itemKeys);
		
		
		return 5;
	}

	@Override
	String doComplete() throws UserErrorMessage
	{
		ds.beginBulkWriteMode();
		CachedEntity character = db.getCurrentCharacter();
		doChecks(character);
		
		ODPKnowledgeService knowledgeService = db.getKnowledgeService(character.getKey());
		ODPInventionService inventionService = db.getInventionService(character, knowledgeService);
		
		EntityPool pool = new EntityPool(ds);
		String message = "";
		
		// Research one of the available items at random
		List<Key> selectedItems = (List<Key>)data.get("selectedItems");
		if (selectedItems==null)
			selectedItems = inventionService.getAvailableItemKeys();
		Collections.shuffle(selectedItems);
		pool.loadEntities(selectedItems);
		
		// Gain some experience with one of the items available
		for(Key itemKey:selectedItems)
		{
			if (knowledgeService.increaseKnowledgeFor(pool.get(itemKey), new Random().nextInt(2)+1))
			{
				message = "You've gained some experience with the "+pool.get(itemKey).getProperty("name")+".";
				break;
			}
		}
		
		// Attempt to learn a new idea
		CachedEntity idea = inventionService.attemptToDiscoverIdea(selectedItems, pool);
		if (idea!=null)
		{
			message="<div class='message-newidea'>You have a new idea sto "+idea.getProperty("name")+"!</div>"+message;
		}
		
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
			throw new UserErrorMessage("You cannot experiment right now. You're currently maning your store.");
		else if ("UNCONSCIOUS".equals(mode))
			throw new UserErrorMessage("You cannot experiment right now. You're currently unconscious, lol.");
		else if ("DEAD".equals(mode))
			throw new UserErrorMessage("You cannot experiment right now. You're DEAD. D:");
		else if ("NORMAL".equals(character.getProperty("mode"))==false)
			throw new UserErrorMessage("You cannot experiment right now. You're too busy.");		
	}
	

}
