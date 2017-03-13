package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.EntityPool;
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
	public List<CachedEntity> getAvailableItems()
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
	public List<Key> getAvailableItemKeys()
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

	public CachedEntity attemptToDiscoverIdea(List<Key> availableItemKeys, EntityPool pool)
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


	public List<CachedEntity> getItemCandidatesFor(EntityPool pool, CachedEntity genericEntityRequirement)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void checkIdeaWithSelectedItems(EntityPool pool, CachedEntity idea, Map<Key, Key> itemRequirementsToItems) throws UserErrorMessage
	{
		// TODO Auto-generated method stub
		
	}

	public CachedEntity createBaseSkillFromIdea(Key key, CachedEntity idea, EntityPool pool)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public CachedEntity createBaseItemFromSkill(CachedEntity skill, EntityPool pool)
	{
		// TODO Auto-generated method stub
		return null;
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

}
