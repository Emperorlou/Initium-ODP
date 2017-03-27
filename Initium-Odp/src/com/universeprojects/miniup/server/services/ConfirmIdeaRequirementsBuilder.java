package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.OperationBase;

public class ConfirmIdeaRequirementsBuilder extends UserRequestBuilder<Map<Key,Key>>
{
	Map<String, Collection<Key>> requirements = new HashMap<>();
	
	CachedEntity idea;
	
	public ConfirmIdeaRequirementsBuilder(String uniqueId, ODPDBAccess db, OperationBase command, CachedEntity idea)
	{
		super(uniqueId, db, command);
		this.idea = idea;
	}
	
	
	public ConfirmIdeaRequirementsBuilder addGenericEntityRequirements(String category, Collection<Key> genericEntityRequirements)
	{
		if (genericEntityRequirements==null) return this;
		
		Collection<Key> gers = requirements.get(category);
		if (gers==null)
		{
			gers = new ArrayList<Key>();
			requirements.put(category, gers);
		}
		
		
		gers.addAll(genericEntityRequirements);
		
		return this;
	}
	
	@Override
	protected Map<Key, Key> convertParametersToResult(JSONObject userResponse)
	{
		Map<Key, Key> result = new HashMap<Key, Key>();
		for(Object key:userResponse.keySet())
		{
			String itemId = userResponse.get(key).toString();
			String gerId = key.toString().substring(18);
	
			if (itemId==null || itemId.length()==0)
				result.put(KeyFactory.createKey("GenericEntityRequirement", Long.parseLong(gerId)), null);
			else
				result.put(KeyFactory.createKey("GenericEntityRequirement", Long.parseLong(gerId)), KeyFactory.createKey("Item", Long.parseLong(itemId)));
		}
		return result;
	}


	@Override
	protected String getPagePopupUrl()
	{
		return "/odp/confirmrequirements?ideaId="+idea.getId();
	}


	@Override
	protected String getPagePopupTitle()
	{
		return "Select the tools/materials to use";
	}


	@Override
	protected String getJavascriptRecallFunction()
	{
		return "doCreatePrototype(null, "+idea.getId()+", '"+idea.getProperty("name").toString().replace("'", "\\'")+"');";
	}

}
