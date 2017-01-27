package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;

public class InventionService extends Service
{
	
	
	final private CachedEntity character;
	
	private List<CachedEntity> availableItems = null;
	
	public InventionService(ODPDBAccess db, CachedEntity character)
	{
		super(db);
		this.character = character;
	}

	
	/**
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
		if (availableItems!=null)
			return availableItems;
		
		availableItems = db.getFilteredList("Item", "containerKey", character.getKey());
		
		availableItems.addAll(db.getFilteredList("Item", "containerKey", character.getProperty("locationKey")));
		
		return availableItems;
	}
	
	/**
	 * This will look at all available items to see if we can find any that match the given entityRequirement.
	 * We return all possible matches.
	 * 
	 * @param entityRequirement
	 * @param excludedItems
	 * @return
	 */
	public List<CachedEntity> getItemCandidatesFor(CachedEntity entityRequirement, Collection<CachedEntity> excludedItems)
	{
		getAvailableItems();
		
		Set<Key> excludedKeys = new HashSet<Key>();
		for(CachedEntity e:excludedItems)
			excludedKeys.add(e.getKey());
		
		List<CachedEntity> results = new ArrayList<CachedEntity>();
		for(CachedEntity candidate:availableItems)
		{
			if (excludedKeys.isEmpty()==false && excludedKeys.contains(candidate.getKey()))
				continue;
			
			if (db.validateEntityRequirement(entityRequirement, candidate))
				results.add(candidate);
		}
		
		return results;
	}
	
	public GenericAffectorResult processGenericAffector(CachedEntity sourceEntity, CachedEntity genericAffector)
	{
		if (genericAffector==null)
			return null;
		if (sourceEntity==null)
			throw new IllegalArgumentException("SourceEntity cannot be null.");
		
		GenericAffectorResult result = null;
		
		String sourceFieldName = (String)genericAffector.getProperty("sourceFieldName");
		Double sourceFieldMinimumValue = (Double)genericAffector.getProperty("sourceFieldMinimumValue");
		Double sourceFieldMaximumValue = (Double)genericAffector.getProperty("sourceFieldMaximumValue");
		
		String destinationFieldName = (String)genericAffector.getProperty("destinationFieldName");
		Double maximumMultiplier = (Double)genericAffector.getProperty("maximumMultiplier");
		Double minimumMultiplier = (Double)genericAffector.getProperty("minimumMultiplier");
		
		if (sourceFieldName==null)
			throw new IllegalArgumentException("sourceFieldName cannot be null. GenericAffector: "+genericAffector.getKey());
		if (sourceFieldMinimumValue==null)
			throw new IllegalArgumentException("sourceFieldMinimumValue cannot be null. GenericAffector: "+genericAffector.getKey());
		if (sourceFieldMaximumValue==null)
			throw new IllegalArgumentException("sourceFieldMaximumValue cannot be null. GenericAffector: "+genericAffector.getKey());

		if (destinationFieldName==null)
			throw new IllegalArgumentException("destinationFieldName cannot be null. GenericAffector: "+genericAffector.getKey());
		if (minimumMultiplier==null)
			throw new IllegalArgumentException("minimumMultiplier cannot be null. GenericAffector: "+genericAffector.getKey());
		if (maximumMultiplier==null)
			throw new IllegalArgumentException("maximumMultiplier cannot be null. GenericAffector: "+genericAffector.getKey());
		
		Object sourceValue = sourceEntity.getProperty(sourceFieldName);
		if (sourceValue==null)
		{
			
		}
		else if ((sourceValue instanceof Double) || (sourceValue instanceof Long))
		{
			// Calculate source multiplier (as though the destination multiplier was 0..1)
			double multiplier = 0d;
			Double val = null;
			if (sourceValue instanceof Long)
				val = ((Long)sourceValue).doubleValue();
			if (sourceFieldMinimumValue<=sourceFieldMaximumValue)
			{
				if (val<sourceFieldMinimumValue) val = sourceFieldMinimumValue;
				if (val>sourceFieldMaximumValue) val = sourceFieldMaximumValue;
			}
			else
			{
				if (val>sourceFieldMinimumValue) val = sourceFieldMinimumValue;
				if (val<sourceFieldMaximumValue) val = sourceFieldMaximumValue;
			}
			
			double range = sourceFieldMaximumValue-sourceFieldMinimumValue;
			multiplier = (val-sourceFieldMinimumValue)/range;
			
			// Now scale the multiplier to fit inside the destination multiplier range
			double destinationRange = maximumMultiplier-minimumMultiplier;
			double destinationMultiplier = (multiplier*destinationRange)+minimumMultiplier;
			
			
			
			result = new GenericAffectorResult();
			result.resultMultiplier = destinationMultiplier;
			result.resultField = destinationFieldName;
		}
		else
		{
			throw new IllegalArgumentException("The sourceValue '"+sourceFieldName+"' is not a supported type: "+sourceValue.getClass().getSimpleName());
		}
		
		return result;
	}
	
	

	
	public class GenericAffectorResult
	{
		public double resultMultiplier = 0d;
		public String resultField = null;
	}
}
