package com.universeprojects.miniup.server.longoperations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParserFactory;
import org.json.simple.parser.JSONServerParser;
import org.json.simple.parser.ParseException;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.EntityPool;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.ODPInventionService;
import com.universeprojects.miniup.server.services.ODPKnowledgeService;

public class LongOperationBeginPrototype extends LongOperation
{

	public LongOperationBeginPrototype(ODPDBAccess db, Map<String, String[]> requestParameters) throws UserErrorMessage
	{
		super(db, requestParameters);
	}

	
	@Override
	public String getPageRefreshJavascriptCall() {
		Long ideaId = (Long)data.get("ideaId");
		return "doBeginPrototype(null, "+ideaId+", selectedItems);";
	}
	
	@Override
	int doBegin(Map<String, String> parameters) throws UserErrorMessage
	{
		Long ideaId = Long.parseLong(parameters.get("ideaId"));
		Key ideaKey = KeyFactory.createKey("ConstructItemIdea", ideaId);
		CachedEntity idea = db.getEntity(ideaKey);
		
		CachedEntity character = db.getCurrentCharacter();
		
		doChecks(character, idea);
		
		Map<Key, Key> itemRequirementsToItems = getSelectedItems(parameters.get("selectedItems"));
		
		ODPKnowledgeService knowledgeService = db.getKnowledgeService(character.getKey());
		ODPInventionService inventionService = db.getInventionService(character, knowledgeService);
		EntityPool pool = new EntityPool(ds);
		
		// Pooling entities...
		pool.addToQueue(itemRequirementsToItems.keySet(), itemRequirementsToItems.values());
		inventionService.poolConstructItemIdea(pool, idea);
		
		// This check will throw a UserErrorMessage if it finds anything off
		inventionService.checkIdeaWithSelectedItems(pool, idea, itemRequirementsToItems);
		
		data.put("selectedItems", itemRequirementsToItems);
		data.put("ideaId", ideaId);
		
		
		return 5;
	}



	@Override
	String doComplete() throws UserErrorMessage
	{
		@SuppressWarnings("unchecked")
		Map<Key, Key> itemRequirementsToItems = (Map<Key, Key>)data.get("selectedItems");
		
		CachedEntity character = db.getCurrentCharacter();
		CachedEntity idea = db.getEntity("ConstructItemIdea", (Long)data.get("ideaId"));
		
		doChecks(character, idea);
		
		ODPKnowledgeService knowledgeService = db.getKnowledgeService(character.getKey());
		ODPInventionService inventionService = db.getInventionService(character, knowledgeService);
		EntityPool pool = new EntityPool(ds);
		
		// Pooling entities...
		pool.addToQueue(itemRequirementsToItems.keySet(), itemRequirementsToItems.values());
		inventionService.poolConstructItemIdea(pool, idea);
		
		// This check will throw a UserErrorMessage if it finds anything off
		inventionService.checkIdeaWithSelectedItems(pool, idea, itemRequirementsToItems);
		
		
		// We're ready to create the final prototype and skill
		
		// Create the skill so we can use it again
		CachedEntity skill = inventionService.createBaseSkillFromIdea(character.getKey(), idea, pool);
		
		// Create the prototype item and put it in the player's inventory
		CachedEntity item = inventionService.createBaseItemFromSkill(skill, pool);
		
		// Give the player a message that points to the skill and the new item he made
		setUserMessage("You have a new skill! You successfully turned your idea of "+idea.getProperty("name")+" into a skill. A prototype of your skill is now in your inventory.<br><br>You created an item: "+GameUtils.renderItem(item));
		
		
		return "Experimentation complete.";
	}

	private void doChecks(CachedEntity character, CachedEntity idea) throws UserErrorMessage
	{
		if (CommonChecks.checkCharacterIsTrading(character))
			throw new UserErrorMessage("You cannot experiment right now. You're currently trading.");
		else if (CommonChecks.checkCharacterIsInCombat(character))
			throw new UserErrorMessage("You cannot experiment right now. You're currently in combat.");
		else if (CommonChecks.checkCharacterIsVending(character))
			throw new UserErrorMessage("You cannot experiment right now. You're currently maning your store.");
		else if (CommonChecks.checkCharacterIsUnconscious(character))
			throw new UserErrorMessage("You cannot experiment right now. You're currently unconscious, lol.");
		else if (CommonChecks.checkCharacterIsDead(character))
			throw new UserErrorMessage("You cannot experiment right now. You're DEAD. D:");
		else if (CommonChecks.checkCharacterIsBusy(character))
			throw new UserErrorMessage("You cannot experiment right now. You're too busy.");
		
		
		if (CommonChecks.checkIdeaIsCharacters(idea, character.getKey())==false)
			throw new UserErrorMessage("The idea you tried to turn into a prototype is stored in another character's brain. Nice try.");
	}
	

	private Map<Key, Key> getSelectedItems(String rawParameter)
	{
		Map<Key, Key> result = new HashMap<Key,Key>();
		
		JSONObject selectedItems = null;
		JSONServerParser jsonParser = JSONParserFactory.getServerParser();
		try
		{
			selectedItems = (JSONObject)jsonParser.parse(rawParameter);
		}
		catch (ParseException e)
		{
			throw new RuntimeException("Failed to parse selectedItems JSON.", e);
		}
		
		for(Object key:selectedItems.keySet())
		{
			//itemForRequirement4836935344586752
			String keyString = key.toString();
			Long entityRequirementId = Long.parseLong(keyString.substring(18, keyString.length()));
			String selectedItemIdStr = selectedItems.get(key).toString();
			if (selectedItemIdStr.equals(""))
			{
				result.put(KeyFactory.createKey("GenericEntityRequirement", entityRequirementId), null);
				continue;
			}
			Long itemId = Long.parseLong(selectedItemIdStr);
			result.put(KeyFactory.createKey("GenericEntityRequirement", entityRequirementId), KeyFactory.createKey("Item",itemId));
		}
		
		return result;
	}
}
