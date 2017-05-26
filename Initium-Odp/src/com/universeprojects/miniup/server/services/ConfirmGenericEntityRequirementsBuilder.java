package com.universeprojects.miniup.server.services;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.json.shared.JSONObject;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.OperationBase;
import com.universeprojects.miniup.server.services.ConfirmGenericEntityRequirementsBuilder.GenericEntityRequirementResult;

public class ConfirmGenericEntityRequirementsBuilder extends UserRequestBuilder<GenericEntityRequirementResult>
{
	List<String> fieldNames = new ArrayList<String>();
	Map<String, Collection<String>> requirements = new HashMap<>();
	Integer repetitionCount = null;
	
	
	CachedEntity entity;
	
	public ConfirmGenericEntityRequirementsBuilder(String uniqueId, ODPDBAccess db, OperationBase command, String jsInitiatingFunctionCall, CachedEntity entity)
	{
		super(uniqueId, db, command, jsInitiatingFunctionCall);
		this.entity = entity;
	}
	
	
	public ConfirmGenericEntityRequirementsBuilder addGenericEntityRequirements(String category, String fieldName)
	{
		List<List<Key>> ger2DList = db.getEntity2DCollectionValueFromEntity(entity, fieldName);
		if (ger2DList==null) return this;
		
		
		Collection<String> gerCategory = requirements.get(category);
		if (gerCategory==null)
		{
			gerCategory = new ArrayList<String>();
			requirements.put(category, gerCategory);
		}
		
		for(int i = 0; i<ger2DList.size(); i++)
		{
			List<Key> gers = ger2DList.get(i);
			if (gers!=null && gers.isEmpty()==false)
			{
				gerCategory.add(fieldName+":"+i);
			}
		}
		
		fieldNames.add(fieldName);
		
		return this;
	}
	
	public ConfirmGenericEntityRequirementsBuilder setRepetitionCount(Integer count)
	{
		this.repetitionCount = count;
		return this;
	}

	
	@Override
	protected GenericEntityRequirementResult convertParametersToResult(JSONObject userResponse)
	{
		GenericEntityRequirementResult result = new GenericEntityRequirementResult();
		Map<Object,Object> slots = (Map<Object,Object>)userResponse.get("slots");
		for(Object key:slots.keySet())
		{
			String itemId = slots.get(key).toString();
			String gerFieldNameAndSlotIndex = key.toString();
	
			if (itemId==null || itemId.length()==0)
				result.slots.put(gerFieldNameAndSlotIndex, null);
			else
				result.slots.put(gerFieldNameAndSlotIndex, KeyFactory.createKey("Item", Long.parseLong(itemId)));
		}
		
		try
		{
			result.repetitionCount = new Integer(userResponse.get("repetitionCount").toString());
		}
		catch(Exception e)
		{
			// Ignore. We'll leave the rep count as null.
		}
		
		return result;
	}

	protected String getRepetitionsUrlParam()
	{
		String maxReps = "";
		if (repetitionCount!=null && repetitionCount>1)
			maxReps = "&maxReps="+repetitionCount;

		return maxReps;
	}

	@Override
	protected String getPagePopupUrl()
	{
		StringBuilder sb = new StringBuilder();
		boolean firstTime = true;
		for(String fieldName:fieldNames)
		{
			if (firstTime)
				firstTime=false;
			else
				sb.append(",");
			
			sb.append(fieldName);
		}
		
		return "/odp/confirmrequirements?entity="+KeyFactory.keyToString(entity.getKey())+"&entityFields="+sb.toString()+getRepetitionsUrlParam();
	}


	@Override
	protected String getPagePopupTitle()
	{
		return "Select the tools/materials to use";
	}

	
	public class GenericEntityRequirementResult implements Serializable
	{
		private static final long serialVersionUID = 5635978106621359645L;
		public Map<String,Key> slots = new HashMap<>();
		public Integer repetitionCount = null; 
	}
}
