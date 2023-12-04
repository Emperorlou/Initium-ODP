package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.EntityPool;
import com.universeprojects.miniup.server.InitiumKey;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public abstract class ODPInventionService extends Service
{
	final protected ODPKnowledgeService knowledgeService;
	
	protected ODPInventionService(ODPDBAccess db, ODPKnowledgeService knowledgeService)
	{
		super(db);
		this.knowledgeService = knowledgeService;
	}
	
	public ODPKnowledgeService getKnowledgeService()
	{
		return knowledgeService;
	}

	/**This is a placeholder. The implementation of this method is in the core repo.
	 * 
	 * This method will look at the character's location, and his inventory to 
	 * find all the items that are available to him. It will sort the items by
	 * inventory first, then location so that other processes can give priority
	 * to things that are on-hand rather than in the environment.
	 * 
	 * The database will only be hit the first time, subsequently a cached result
	 * will be returned.
	 * 
	 * @return
	 */
	public List<CachedEntity> getAvailableItems(Long tileX, Long tileY)
	{
		return null;
	}

	/**This is a placeholder. The implementation of this method is in the core repo.
	 * 
	 * This method will look at the character's location, and his inventory to 
	 * find all the items that are available to him. It will sort the items by
	 * inventory first, then location so that other processes can give priority
	 * to things that are on-hand rather than in the environment.
	 * 
	 * The database will only be hit the first time, subsequently a cached result
	 * will be returned.
	 * 
	 * @return
	 */
	public List<Object> getAvailableItemKeys(Long tileX, Long tileY)
	{
		return null;
	}

	public List<CachedEntity> getAllItemConstructionIdeas()
	{
		return null;
	}
	
	public Map<Key, CachedEntity> getAllItemDefsForItemConstructionIdeas()
	{
		return null;
	}

	public GenericAffectorResult processGenericAffector(CachedEntity sourceEntity, CachedEntity genericAffector)
	{
		// TODO Auto-generated method stub
		return null;
	}

	
	
	
	public class GenericEntityRequirementResult
	{
		public List<GenericAffectorResult> processAffectors = new ArrayList<GenericAffectorResult>();
		public List<GenericAffectorResult> resultingFieldAffectors = new ArrayList<GenericAffectorResult>();
		
	}
	
	public class GenericAffectorResult
	{
		public Double resultMultiplier = 0d;
		public String resultField = null;
	}

	public boolean validateEntityRequirement(CachedEntity entityRequirement, CachedEntity entity)
	{
		// TODO Auto-generated method stub
		return false;
	}

	

	public CachedEntity createBaseIdeaFromIdeaDef(Key characterKey, CachedEntity ideaDef, EntityPool pool)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public CachedEntity attemptToDiscoverIdea(Long tileX, Long tileY, List availableItemKeys, EntityPool pool)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public List<Key> getAllItemConstructionIdeaKeys()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void poolConstructItemIdea(EntityPool pool, CachedEntity entity)
	{
		// TODO Auto-generated method stub
		
	}

	public void poolGenericEntityRequirement(EntityPool pool, Object genericEntityRequirementListFieldValue)
	{
		// TODO Auto-generated method stub
		
	}


	public List<CachedEntity> getItemCandidatesFor(Long tileX, Long tileY, EntityPool pool, CachedEntity genericEntityRequirement)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void checkIdeaWithSelectedItems(EntityPool pool, CachedEntity idea, Map<Key, List<InitiumKey>> itemRequirementsToItems) throws UserErrorMessage
	{
		// TODO Auto-generated method stub
		
	}


	public List<CachedEntity> getAllItemConstructionSkills()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public List<Key> getAllItemConstructionSkillKeys()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void poolConstructItemSkill(EntityPool pool, CachedEntity entity)
	{
		// TODO Auto-generated method stub
		
	}

	public Map<Key, CachedEntity> getAllItemsForItemConstructionSkills()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void checkSkillWithSelectedItems(EntityPool pool, CachedEntity skill, Map<Key, List<InitiumKey>> itemRequirementsToItems, Integer repetitionCount) throws UserErrorMessage
	{
		// TODO Auto-generated method stub
		
	}

	public CachedEntity doCreateConstructItemPrototype(CachedEntity idea, Map<Key, List<InitiumKey>> itemRequirementsToItems, EntityPool pool, Integer repetitionCount) throws UserErrorMessage
	{
		// TODO Auto-generated method stub
		return null;
	}

	public CachedEntity doConstructItemSkill(CachedEntity skill, Map<Key, List<InitiumKey>> itemRequirementsToItems, EntityPool pool, Integer repetitionCount) throws UserErrorMessage
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void processConstructItemSkillForProcessVariables(CachedEntity skill, Map<Key, List<InitiumKey>> itemRequirementsToItems, Map<String, Object> processVariables, EntityPool pool) throws UserErrorMessage
	{
		// TODO Auto-generated method stub
		
	}

	public Set<CachedEntity> getItemCandidatesFor(Long tileX, Long tileY, EntityPool pool, List<CachedEntity> genericEntityRequirementsList)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Map<Key, List<InitiumKey>> resolveGerSlotsToGers(EntityPool pool, CachedEntity entity, Map<String, List<InitiumKey>> selectedItems, Integer repetitionCount) throws UserErrorMessage
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void poolGerSlotsAndSelectedItems(EntityPool pool, CachedEntity entity, Map<String, List<InitiumKey>> selectedItems)
	{
		// TODO Auto-generated method stub
		
	}

	public void checkGersMatchItems(EntityPool pool, Map<Key, List<InitiumKey>> gerToItemMap, Integer repetitionCount) throws UserErrorMessage
	{
		// TODO Auto-generated method stub
		
	}

	public void checkRequiredItemsAreAccountedFor(Object ideagenericEntityRequirementFieldValue, Map<Key, List<InitiumKey>> itemRequirementsToItems, String categoryName) throws UserErrorMessage
	{
		// TODO Auto-generated method stub
		
	}

	public void processCollectableResult(EntityPool pool, CachedEntity collectableDef, Map<Key, List<InitiumKey>> itemRequirementsToItems, CachedEntity resultingItem, Integer repetitionCount)
			throws UserErrorMessage
	{
		// TODO Auto-generated method stub
		
	}

	public void beginCollectableProcess(CachedEntity collectableDef, Map<Key, List<InitiumKey>> itemRequirementsToItems, Map<String, Object> processVariables, EntityPool pool) throws UserErrorMessage
	{
		// TODO Auto-generated method stub
		
	}

	public void poolItemRequirementsToItems(EntityPool pool, Map<Key, List<InitiumKey>> itemRequirementsToItems)
	{
		// TODO Auto-generated method stub
		
	}

	public void modifyFieldsOn(Object genericEntityRequirementFieldValue, Map<Key, List<InitiumKey>> itemRequirementsToItems, EntityPool pool, Integer repetitionCount)
	{
		// TODO Auto-generated method stub
		
	}


	public Collection<Key> getDeletedEntities()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public long getTotalQuantity(List<CachedEntity> items)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public void consumeItems(List<CachedEntity> selectedItems, Long quantityRequired, String gerName) throws UserErrorMessage
	{
		// TODO Auto-generated method stub
		
	}

	public void useItem(CachedEntity selectedItem, Long durabilityToLose)
	{
		// TODO Auto-generated method stub
		
	}

	public void processPrototypeItemSkillForProcessVariables(CachedEntity ideaDef, Map<Key, List<InitiumKey>> itemRequirementsToItems, Map<String, Object> processVariables, EntityPool pool) throws UserErrorMessage
	{
		// TODO Auto-generated method stub
		
	}

	public boolean doIfExpressionCheck(String value1, String operator, Object value2)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public List<CachedEntity> getRelatedSkills(EntityPool pool, CachedEntity materialTool)
	{
		// TODO Auto-generated method stub
		return null;
	}


}
