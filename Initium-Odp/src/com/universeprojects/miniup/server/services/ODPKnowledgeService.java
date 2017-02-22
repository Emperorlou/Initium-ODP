package com.universeprojects.miniup.server.services;

import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;

public abstract class ODPKnowledgeService extends Service
{
	final protected Key characterKey;

	/**
	 * This will query for all knowledge entities associated with the character. 
	 * 
	 * This method will return null only if the number of knowledge entities is
	 * 100 or more. In this case, other techniques will need to be used to fetch
	 * the knowledge tree (like only fetching in parts). This limit is imposed 
	 * for UX purposes.
	 * 
	 * @return
	 */
	public ODPKnowledgeService(ODPDBAccess db, Key characterKey)
	{
		super(db);
		this.characterKey = characterKey;
	}

	
	/**
	 * This will query for all knowledge entities associated with the character. 
	 * 
	 * This method will return null only if the number of knowledge entities is
	 * 100 or more. In this case, other techniques will need to be used to fetch
	 * the knowledge tree (like only fetching in parts). This limit is imposed 
	 * for UX purposes.
	 * 
	 * @return
	 */
	public List<CachedEntity> getAllKnowledge()
	{
		// TODO Auto-generated method stub
		return null;
	}



	/**
	 * This method will return the ancestors and any related knowledge entities that apply
	 * when the given entity is used by the character. 
	 * 
	 * This method currently supports the following entity types: Item
	 * 
	 * @param entity Currently this must be an Item entity but this will likely be expanded.
	 * @return
	 */
	public List<CachedEntity> getKnowledgeEntityFor(CachedEntity entity, boolean createIfNoExist)
	{
		// TODO Auto-generated method stub
		return null;
	}


	/**
	 * This method will add the given amount of experience points to all the knowledge entities associated with
	 * the given entity.
	 * 
	 * This method currently supports the following entity types: Item
	 * 
	 * @param entity Currently this must be an Item entity but this will likely be expanded.
	 * @param amount The number of experience points to add to each knowledge entity found associated with
	 * @return Returns true if the increase was successful, false if it wasn't. It will only return false if the entity is not properly configured for learning (like if there is no itemClass specified on an Item).
	 */	
	public boolean increaseKnowledgeFor(CachedEntity entity, int amount)
	{
		return false;
	}


	/**
	 * This method will return the ancestors and any related knowledge entity keys that apply
	 * when the given entity is used by the character. 
	 * 
	 * This method currently supports the following entity types: Item
	 * 
	 * In this version of the method no additional database calls are made.
	 * 
	 * @param entity Currently this must be an Item entity but this will likely be expanded.
	 * @return
	 */
	public List<Key> getKnowledgeKeyFor(CachedEntity entity)
	{
		// TODO Auto-generated method stub
		return null;
	}


	/**
	 * Gets the knowledge entity that matches the given entity requirement.
	 * 
	 * 
	 * @param inventionService
	 * @param entityRequirement
	 * @return Null will be returned if no knowledge entity is found. Otherwise, all knowledge entities that match the requirement will be returned.
	 */
	public List<CachedEntity> fetchKnowledgeEntityByEntityRequirement(ODPInventionService inventionService, CachedEntity entityRequirement)
	{
		// TODO Auto-generated method stub
		return null;
	}





	

}
