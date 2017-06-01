package com.universeprojects.miniup.server.longoperations;

import java.util.HashMap;
import java.util.Map;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.EntityPool;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.ConfirmSkillRequirementsBuilder;
import com.universeprojects.miniup.server.services.ODPInventionService;
import com.universeprojects.miniup.server.services.ODPKnowledgeService;
import com.universeprojects.miniup.server.services.ConfirmGenericEntityRequirementsBuilder.GenericEntityRequirementResult;

public class LongOperationDoSkillConstructItem extends LongOperation
{

	public LongOperationDoSkillConstructItem(ODPDBAccess db, Map<String, String[]> requestParameters) throws UserErrorMessage
	{
		super(db, requestParameters);
	}
	
	

	@Override
	int doBegin(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException
	{
		Long skillId = Long.parseLong(parameters.get("skillId"));
		Key skillKey = KeyFactory.createKey("ConstructItemSkill", skillId);
		CachedEntity skill = db.getEntity(skillKey);
		if (skill==null)
			throw new UserErrorMessage("Invalid skill specified.");
		
		CachedEntity character = db.getCurrentCharacter();
		
		doChecks(character, skill);

		setDataProperty("skillId", skillId);
		setDataProperty("skillName", skill.getProperty("name"));
		
		CachedEntity ideaDef = db.getEntity((Key)skill.getProperty("_definitionKey"));

		Integer maxRepCount = null;
		if (ideaDef.getProperty("skillMaxRepeat")!=null)
			maxRepCount = ((Long)ideaDef.getProperty("skillMaxRepeat")).intValue();
		
		GenericEntityRequirementResult itemRequirementSlotsToItems = new ConfirmSkillRequirementsBuilder("1", db, this, ideaDef, skill)
		.addGenericEntityRequirements("", "skillItemFocus")
		.addGenericEntityRequirements("Required Materials", "skillMaterialsRequired")
		.addGenericEntityRequirements("Optional Materials", "skillMaterialsOptional")
		.addGenericEntityRequirements("Required Tools/Equipment", "skillToolsRequired")
		.addGenericEntityRequirements("Optional Tools/Equipment", "skillToolsOptional")
		.setRepetitionCount(maxRepCount)
		.go();
		if (itemRequirementSlotsToItems.repetitionCount==null)
			itemRequirementSlotsToItems.repetitionCount = 1; 
		

		
		ODPKnowledgeService knowledgeService = db.getKnowledgeService(character.getKey());
		ODPInventionService inventionService = db.getInventionService(character, knowledgeService);
		EntityPool pool = new EntityPool(ds);
		
		
		
		// Pooling entities...
		pool.addEntityDirectly(ideaDef);
		inventionService.poolConstructItemSkill(pool, skill);
		inventionService.poolGerSlotsAndSelectedItems(pool, ideaDef, itemRequirementSlotsToItems.slots);
		
		pool.loadEntities();
		
		
		
		// Now figure out which of the gers in each slot should actually be used
		Map<Key, Key> itemRequirementsToItems = inventionService.resolveGerSlotsToGers(pool, ideaDef, itemRequirementSlotsToItems.slots, itemRequirementSlotsToItems.repetitionCount);
		

		// This check will throw a UserErrorMessage if it finds anything off
		inventionService.checkSkillWithSelectedItems(pool, skill, itemRequirementsToItems, itemRequirementSlotsToItems.repetitionCount);

		Map<String,Object> processVariables = new HashMap<String,Object>();
		Long seconds = 5L;
		if (skill.getProperty("skillConstructionSpeed")!=null)
			seconds = ((Long)skill.getProperty("skillConstructionSpeed")).longValue();
		
		
		processVariables.put("speed", seconds);

		// Add in the repetitions...
		if (itemRequirementSlotsToItems.repetitionCount!=null)
			seconds*=itemRequirementSlotsToItems.repetitionCount;
		
		inventionService.processConstructItemSkillForProcessVariables(skill, itemRequirementsToItems, processVariables, pool);
		
		
		seconds = (Long)processVariables.get("speed");
		if (itemRequirementSlotsToItems.repetitionCount!=null)
			seconds*=itemRequirementSlotsToItems.repetitionCount;
		
		setDataProperty("selectedItems", itemRequirementsToItems);
		setDataProperty("repetitionCount", itemRequirementSlotsToItems.repetitionCount);
		
		
		setDataProperty("description", "It will take "+seconds+" seconds to finish this construction.");
		
		// Issue the soundeffect for the location if one is necessary..
		if (ideaDef.getProperty("executionSoundeffect")!=null)
		{
			db.sendSoundEffectToLocation(ds, (Key)character.getProperty("locationKey"), (String)ideaDef.getProperty("executionSoundeffect"));
		}
		
		return seconds.intValue();
	}



	@Override
	String doComplete() throws UserErrorMessage, UserRequestIncompleteException
	{
		@SuppressWarnings("unchecked")
		Map<Key, Key> itemRequirementsToItems = (Map<Key, Key>)getDataProperty("selectedItems");
		Integer repetitionCount = (Integer)getDataProperty("repetitionCount");
		
		CachedEntity character = db.getCurrentCharacter();
		CachedEntity skill = db.getEntity("ConstructItemSkill", (Long)getDataProperty("skillId"));
		
		doChecks(character, skill);
		
		ODPKnowledgeService knowledgeService = db.getKnowledgeService(character.getKey());
		ODPInventionService inventionService = db.getInventionService(character, knowledgeService);
		EntityPool pool = new EntityPool(ds);
		
		CachedEntity ideaDef = db.getEntity((Key)skill.getProperty("_definitionKey"));
		
		// Pooling entities...
		pool.addEntityDirectly(ideaDef);
		inventionService.poolConstructItemSkill(pool, skill);
		pool.addToQueue(itemRequirementsToItems.keySet(), itemRequirementsToItems.values());
		
		pool.loadEntities();
		
		
		// We're ready to create the final prototype and skill
		
		// Create the skill so we can use it again
		CachedEntity item = inventionService.doConstructItemSkill(skill, itemRequirementsToItems, pool, repetitionCount);
		
		// Now add to the knowledge we gain
		knowledgeService.increaseKnowledgeFor(skill, 1, 100);
		knowledgeService.increaseKnowledgeFor(item, 1, 100);
		
		// Give the player a message that points to the skill and the new item he made
		String msg = "You created an item: "+GameUtils.renderItem(item);
		setUserMessage(msg);
		
		// Delete all HTML of an item
		if (inventionService.getDeletedEntities()!=null)
			for(Key deletedKey:inventionService.getDeletedEntities())
				if (deletedKey.getKind().equals("Item"))
					deleteHtml(".deletable-Item"+deletedKey.getId());
		
		
		return "Skill complete. "+msg;
	}

	@Override
	public String getPageRefreshJavascriptCall() {
		Long skillId = (Long)getDataProperty("skillId");
		String skillName = ((String)getDataProperty("skillName")).replace("'", "\\'");
		return "doConstructItemSkill(null, "+skillId+", '"+skillName+"');"; 
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
			throw new UserErrorMessage("The item construction skill you tried to use is stored in another character's brain. Nice try.");
	}
	
}
