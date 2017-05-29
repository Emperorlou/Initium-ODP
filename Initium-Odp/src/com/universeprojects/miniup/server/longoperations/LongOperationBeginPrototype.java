package com.universeprojects.miniup.server.longoperations;

import java.util.HashMap;
import java.util.Map;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.EntityPool;
import com.universeprojects.json.shared.JSONObject;
import com.universeprojects.json.shared.parser.JSONParserFactory;
import com.universeprojects.json.shared.parser.JSONServerParser;
import com.universeprojects.json.shared.parser.ParseException;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.ConfirmGenericEntityRequirementsBuilder;
import com.universeprojects.miniup.server.services.ConfirmGenericEntityRequirementsBuilder.GenericEntityRequirementResult;
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
		Long ideaId = (Long)getDataProperty("ideaId");
		String ideaName = ((String)getDataProperty("ideaName")).replace("'", "\\'");
		return "doCreatePrototype(null, "+ideaId+", '"+ideaName+"');"; 
	}
	
	
	
	@SuppressWarnings("unchecked")
	int doBegin(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException 
	{
		Long ideaId = Long.parseLong(parameters.get("ideaId"));
		Key ideaKey = KeyFactory.createKey("ConstructItemIdea", ideaId);
		CachedEntity idea = db.getEntity(ideaKey);
		if (idea==null)
			throw new UserErrorMessage("Invalid idea specified.");

		CachedEntity character = db.getCurrentCharacter();
		
		setDataProperty("ideaId", ideaId);
		setDataProperty("ideaName", idea.getProperty("name"));
		
		
		
		doChecks(character, idea);

		
		CachedEntity ideaDef = db.getEntity((Key)idea.getProperty("_definitionKey"));

		
		GenericEntityRequirementResult itemRequirementSlotsToItems = new ConfirmGenericEntityRequirementsBuilder("1", db, this, getPageRefreshJavascriptCall(), ideaDef)
		.addGenericEntityRequirements("", "skillItemFocus")
		.addGenericEntityRequirements("Required Materials", "skillMaterialsRequired")
		.addGenericEntityRequirements("Required Materials", "prototypeItemsConsumed")
		.addGenericEntityRequirements("Optional Materials", "skillMaterialsOptional")
		.addGenericEntityRequirements("Required Tools/Equipment", "skillToolsRequired")
		.addGenericEntityRequirements("Required Tools/Equipment", "prototypeItemsRequired")
		.addGenericEntityRequirements("Optional Tools/Equipment", "skillToolsOptional")
		.go();
		if (itemRequirementSlotsToItems.repetitionCount==null)
			itemRequirementSlotsToItems.repetitionCount = 1; 
		
		ODPKnowledgeService knowledgeService = db.getKnowledgeService(character.getKey());
		ODPInventionService inventionService = db.getInventionService(character, knowledgeService);
		EntityPool pool = new EntityPool(ds);

		
		
		// Pooling entities...
		pool.addEntityDirectly(ideaDef);
		inventionService.poolConstructItemIdea(pool, idea);
		inventionService.poolGerSlotsAndSelectedItems(pool, ideaDef, itemRequirementSlotsToItems.slots);
		
		pool.loadEntities();
		
		
		// Now figure out which of the gers in each slot should actually be used
		Map<Key, Key> itemRequirementsToItems = inventionService.resolveGerSlotsToGers(pool, ideaDef, itemRequirementSlotsToItems.slots, itemRequirementSlotsToItems.repetitionCount);
		
		
		// This check will throw a UserErrorMessage if it finds anything off
		inventionService.checkIdeaWithSelectedItems(pool, ideaDef, itemRequirementsToItems);
		
		setDataProperty("selectedItems", itemRequirementsToItems);
		setDataProperty("repetitionCount", itemRequirementSlotsToItems.repetitionCount);
		
		int seconds = 5;
		
		if (ideaDef.getProperty("prototypeConstructionSpeed")!=null)
			seconds = ((Long)ideaDef.getProperty("prototypeConstructionSpeed")).intValue();
		
		// Add in the repetitions...
		if (itemRequirementSlotsToItems.repetitionCount!=null)
			seconds*=itemRequirementSlotsToItems.repetitionCount;
		
		setDataProperty("description", "It will take "+seconds+" seconds to finish this prototype.");
		
		// Issue the soundeffect for the location if one is necessary..
		if (ideaDef.getProperty("executionSoundeffect")!=null)
		{
			db.sendSoundEffectToLocation(ds, (Key)character.getProperty("locationKey"), (String)ideaDef.getProperty("executionSoundeffect"));
		}
		
		return seconds;
	}



	@Override
	String doComplete() throws UserErrorMessage, UserRequestIncompleteException
	{
		@SuppressWarnings("unchecked")
		CachedEntity character = db.getCurrentCharacter();
		Map<Key,Key> itemRequirementsToItems = (Map<Key, Key>)getDataProperty("selectedItems");
		Integer repetitionCount = (Integer)getDataProperty("repetitionCount");
		
		CachedEntity idea = db.getEntity("ConstructItemIdea", (Long)getDataProperty("ideaId"));
		
		doChecks(character, idea);
		
		ODPKnowledgeService knowledgeService = db.getKnowledgeService(character.getKey());
		ODPInventionService inventionService = db.getInventionService(character, knowledgeService);
		EntityPool pool = new EntityPool(ds);
		
		// Pooling entities...
		pool.addToQueue(itemRequirementsToItems.keySet(), itemRequirementsToItems.values());
		
		
		// We're ready to create the final prototype and skill
		
		// Create the skill so we can use it again
		CachedEntity item = inventionService.doCreateConstructItemPrototype(idea, itemRequirementsToItems, pool, repetitionCount);

		// Now add to the knowledge we gain
		knowledgeService.increaseKnowledgeFor(idea, 2, 100);
		knowledgeService.increaseKnowledgeFor(item, 1, 100);
		
		
		// Give the player a message that points to the skill and the new item he made
		setUserMessage("You have a new skill! You successfully turned your idea of "+idea.getProperty("name")+" into a skill. A prototype of your skill is now in your inventory.<br><br>You created an item: "+GameUtils.renderItem(item));
		
		// Delete all HTML of an item
		if (inventionService.getDeletedEntities()!=null)
			for(Key deletedKey:inventionService.getDeletedEntities())
				if (deletedKey.getKind().equals("Item"))
					deleteHtml(".deletable-Item"+deletedKey.getId());
		
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
	
	@Override
	public Map<String, Object> getStateData() {
		Map<String, Object> stateData = super.getStateData();
		
		stateData.put("description", getDataProperty("description"));
		
		return stateData;
	}	
}
