package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.EntityPool;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.services.ODPInventionService;
import com.universeprojects.miniup.server.services.ODPKnowledgeService;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

@Controller
public class ConfirmRequirementsController extends PageController
{
	public enum Type
	{
		IdeaToPrototype 
	}
	
	public ConfirmRequirementsController()
	{
		super("confirmrequirements");
	}

	@Override
	protected String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Type type = determineType(request);
		
		ODPDBAccess db = ODPDBAccess.getInstance(request);

		request.setAttribute("type", type.toString());
		if (type == Type.IdeaToPrototype)
		{
			processForCreatePrototype(request, db);
		}
		
		
	    return "/WEB-INF/odppages/ajax_confirmrequirements.jsp";
	}

	private void addGenericEntityRequirements(HttpServletRequest request, EntityPool pool, String categoryName, Object genericRequirementKeysList)
	{
		if (genericRequirementKeysList==null)
			return;
		if ((genericRequirementKeysList instanceof List)==false)
			throw new IllegalArgumentException("The genericRequirementKeysList type is not a list.");
		
		for(Object gerKeyObj:(List<?>)genericRequirementKeysList)
		{
			if ((gerKeyObj instanceof Key)==false)
				throw new IllegalArgumentException("The genericRequirementKeysList was a list but contained objects that were not Key types.");
			addGenericEntityRequirement(request, categoryName, pool.get((Key)gerKeyObj));
		}
	}
	
	private void addGenericEntityRequirement(HttpServletRequest request, String categoryName, CachedEntity genericRequirement)
	{
		if (categoryName==null) throw new IllegalArgumentException("categoryName cannot be null.");
		
		List<Map<String, Object>> formattedRequirementsCategories = (List<Map<String, Object>>)request.getAttribute("formattedRequirements");
		if (formattedRequirementsCategories == null)
			formattedRequirementsCategories = new ArrayList<Map<String, Object>>();
		
		// Get by category...
		Map<String, Object> formattedRequirementsCategory = null;
		for(Map<String,Object> category:formattedRequirementsCategories)
			if (categoryName.equals(category.get("name")))
			{
				formattedRequirementsCategory = category;
				break;
			}
		if (formattedRequirementsCategory==null)
		{
			formattedRequirementsCategory = new HashMap<String,Object>();
			formattedRequirementsCategory.put("name", categoryName);
			formattedRequirementsCategory.put("list", new ArrayList<Map<String,String>>());
			formattedRequirementsCategories.add(formattedRequirementsCategory);
		}
		
		// Now add the formatted entity requirements...
		List<Map<String,String>> formattedEntityRequirements = (List<Map<String,String>>)formattedRequirementsCategory.get("list");

		Map<String, String> fer = new HashMap<String, String>();	// fer = Formatted Entity Requirement
		
		// Formatting the output
		fer.put("name", (String)genericRequirement.getProperty("name"));
		fer.put("description", (String)genericRequirement.getProperty("description"));
		fer.put("id", genericRequirement.getId().toString());
		
		
		formattedEntityRequirements.add(fer);
		
		
		request.setAttribute("formattedRequirements", formattedRequirementsCategories);
		
	}

	private void processForCreatePrototype(HttpServletRequest request, ODPDBAccess db)
	{
		CachedEntity character = db.getCurrentCharacter(); 
		CachedDatastoreService ds = db.getDB();
		EntityPool pool = new EntityPool(ds);
		Long ideaId = Long.parseLong(request.getParameter("ideaId"));
		CachedEntity idea = db.getEntity(KeyFactory.createKey("ConstructItemIdea", ideaId));
		
		// Make sure the idea we're processing is actually owned by the character who is executing it
		if (GameUtils.equals(idea.getProperty("characterKey"), character.getKey())==false)
			throw new IllegalArgumentException("Possible hack attempt. An ideaId from a different character was used.");
		
		ODPKnowledgeService knowledgeService = db.getKnowledgeService(character.getKey());
		ODPInventionService invention = db.getInventionService(character, knowledgeService);

		// Load all the generic entity requirements (but not the subentities. That's why I didn't use inventionservice.poolConstructItemIdea())
		pool.addToQueue(idea.getProperty("skillMaterialsRequired"));
		pool.addToQueue(idea.getProperty("prototypeItemsConsumed"));
		pool.addToQueue(idea.getProperty("skillMaterialsOptional"));
		pool.addToQueue(idea.getProperty("skillToolsRequired"));
		pool.addToQueue(idea.getProperty("prototypeItemsRequired"));
		pool.addToQueue(idea.getProperty("skillToolsOptional"));

		pool.loadEntities();
		
		addGenericEntityRequirements(request, pool, "Required Materials", idea.getProperty("skillMaterialsRequired"));
		addGenericEntityRequirements(request, pool, "Required Materials", idea.getProperty("prototypeItemsConsumed"));
		addGenericEntityRequirements(request, pool, "Optional Materials", idea.getProperty("skillMaterialsOptional"));
		addGenericEntityRequirements(request, pool, "Required Tools/Equipment", idea.getProperty("skillToolsRequired"));
		addGenericEntityRequirements(request, pool, "Required Tools/Equipment", idea.getProperty("prototypeItemsRequired"));
		addGenericEntityRequirements(request, pool, "Optional Tools/Equipment", idea.getProperty("skillToolsOptional"));
		
	}
	
	
	
	
	private List<Key> rawKeysToKeys(String[] rawKeys)
	{
		List<Key> result = new ArrayList<Key>();
		for(String rawKey:rawKeys)
		{
			if (rawKey.endsWith("\")"))
			{
				// Keys using names
				String[] parts = rawKey.split("\\(\"");
				if (parts.length!=2)
					throw new IllegalArgumentException("Key was malformed: "+rawKey);
				
				String kind = parts[0];
				String name = parts[1].substring(0, parts[1].length()-2);
				
				result.add(KeyFactory.createKey(kind, name));
			}
			else
			{
				// Keys using IDs
				String[] parts = rawKey.split("\\(");
				if (parts.length!=2)
					throw new IllegalArgumentException("Key was malformed: "+rawKey);
				
				String kind = parts[0];
				String idStr = parts[1].substring(0, parts[1].length()-1);
				Long id = Long.parseLong(idStr);
				
				result.add(KeyFactory.createKey(kind, id));
			}
		}
		return result;
	}
	
	private Type determineType(HttpServletRequest request)
	{
		if (request.getParameterMap().containsKey("ideaId"))
			return Type.IdeaToPrototype;
		
		throw new IllegalArgumentException("Unable to determine type for confirm requirements page.");
	}
}

