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

		data.put("skillId", skillId);
		data.put("skillName", skill.getProperty("name"));
		
		CachedEntity ideaDef = db.getEntity((Key)skill.getProperty("_definitionKey"));

		Map<String, Key> itemRequirementSlotsToItems = new ConfirmSkillRequirementsBuilder("1", db, this, ideaDef, skill)
		.addGenericEntityRequirements("Required Materials", "skillMaterialsRequired")
		.addGenericEntityRequirements("Optional Materials", "skillMaterialsOptional")
		.addGenericEntityRequirements("Required Tools/Equipment", "skillToolsRequired")
		.addGenericEntityRequirements("Optional Tools/Equipment", "skillToolsOptional")
		.go();
		

		
		ODPKnowledgeService knowledgeService = db.getKnowledgeService(character.getKey());
		ODPInventionService inventionService = db.getInventionService(character, knowledgeService);
		EntityPool pool = new EntityPool(ds);
		
		
		
		// Pooling entities...
		pool.addEntityDirectly(ideaDef);
		inventionService.poolConstructItemSkill(pool, skill);
		inventionService.poolGerSlotsAndSelectedItems(pool, ideaDef, itemRequirementSlotsToItems);
		
		pool.loadEntities();
		
		
		
		// Now figure out which of the gers in each slot should actually be used
		Map<Key, Key> itemRequirementsToItems = inventionService.resolveGerSlotsToGers(pool, ideaDef, itemRequirementSlotsToItems);
		
		
		// This check will throw a UserErrorMessage if it finds anything off
		inventionService.checkSkillWithSelectedItems(pool, skill, itemRequirementsToItems);

		Map<String,Object> processVariables = new HashMap<String,Object>();
		Long seconds = 5L;
		if (skill.getProperty("skillConstructionSpeed")!=null)
			seconds = ((Long)skill.getProperty("skillConstructionSpeed")).longValue();
		processVariables.put("speed", seconds);
		
		inventionService.processConstructItemSkillForProcessVariables(skill, itemRequirementsToItems, processVariables, pool);
		
		seconds = (Long)processVariables.get("speed");
		
		data.put("selectedItems", itemRequirementsToItems);
		
		
		data.put("description", "It will take "+seconds+" seconds to finish this construction.");
		
		return seconds.intValue();
	}



	@Override
	String doComplete() throws UserErrorMessage, UserRequestIncompleteException
	{
		@SuppressWarnings("unchecked")
		Map<Key, Key> itemRequirementsToItems = (Map<Key, Key>)data.get("selectedItems");
		
		CachedEntity character = db.getCurrentCharacter();
		CachedEntity skill = db.getEntity("ConstructItemSkill", (Long)data.get("skillId"));
		
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
		CachedEntity item = inventionService.doConstructItemSkill(skill, itemRequirementsToItems, pool);
		
		// Now add to the knowledge we gain
		knowledgeService.increaseKnowledgeFor(skill, 1);
		knowledgeService.increaseKnowledgeFor(item, 1);
		
		// Give the player a message that points to the skill and the new item he made
		setUserMessage("You created an item: "+GameUtils.renderItem(item));
		
		
		return "Skill complete.";
	}

	@Override
	public String getPageRefreshJavascriptCall() {
		Long skillId = (Long)data.get("skillId");
		String skillName = ((String)data.get("skillName")).replace("'", "\\'");
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
