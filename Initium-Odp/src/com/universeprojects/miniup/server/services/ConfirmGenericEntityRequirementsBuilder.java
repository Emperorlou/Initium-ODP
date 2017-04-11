package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.OperationBase;

public class ConfirmGenericEntityRequirementsBuilder extends UserRequestBuilder<Map<String,Key>>
{
	List<String> fieldNames = new ArrayList<String>();
	Map<String, Collection<String>> requirements = new HashMap<>();
	
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
	
	@Override
	protected Map<String, Key> convertParametersToResult(JSONObject userResponse)
	{
		Map<String, Key> result = new HashMap<>();
		for(Object key:userResponse.keySet())
		{
			String itemId = userResponse.get(key).toString();
			String gerFieldNameAndSlotIndex = key.toString();
	
			if (itemId==null || itemId.length()==0)
				result.put(gerFieldNameAndSlotIndex, null);
			else
				result.put(gerFieldNameAndSlotIndex, KeyFactory.createKey("Item", Long.parseLong(itemId)));
		}
		return result;
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
		return "/odp/confirmrequirements?entity="+KeyFactory.keyToString(entity.getKey())+"&entityFields="+sb.toString();
	}


	@Override
	protected String getPagePopupTitle()
	{
		return "Select the tools/materials to use";
	}

}
