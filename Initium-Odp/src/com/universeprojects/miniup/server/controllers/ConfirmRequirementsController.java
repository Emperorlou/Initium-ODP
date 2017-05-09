package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.EntityPool;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

@Controller
public class ConfirmRequirementsController extends PageController
{
	public enum Type
	{
		IdeaToPrototype,
		ConstructItemSkill,
		CollectCollectable,
		GenericCommand,
		GenericLongOperation
	}
	
	public ConfirmRequirementsController()
	{
		super("confirmrequirements");
	}

	@Override
	protected String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		request.setAttribute("userRequestId", request.getParameter("userRequestId"));
		
		Type type = determineType(request);
		
		ODPDBAccess db = ODPDBAccess.getInstance(request);

		
		if (type == Type.IdeaToPrototype)
		{
			processForCreatePrototype(request, db);
		}
		else if (type == Type.ConstructItemSkill)
		{
			processForConstructItemSkill(request, db);
		}
		else if (type == Type.GenericCommand || type == Type.GenericLongOperation)
		{
			processForGenericCommand(request, db);
		}
		else if (type == Type.CollectCollectable)
		{
			processForGenericCommand(request, db);
		}
		
		request.setAttribute("type", type.toString());
		if (request.getParameter("maxReps")!=null && request.getParameter("maxReps").length()>0)
			request.setAttribute("maxReps", request.getParameter("maxReps"));
		
	    return "/WEB-INF/odppages/ajax_confirmrequirements.jsp";
	}

	private void addGenericEntityRequirements(HttpServletRequest request, ODPDBAccess db, EntityPool pool, String categoryName, CachedEntity entity, String ger2DCollectionFieldName)
	{
		List<List<Key>> entity2dList = db.getEntity2DCollectionValueFromEntity(entity, ger2DCollectionFieldName);
		
		if (entity2dList==null)
			return;
		
		int slotIndex = 0;
		for(List<Key> gerSlotList:entity2dList)
		{
			addGenericEntityRequirementSlot(request, pool, categoryName, slotIndex, ger2DCollectionFieldName, gerSlotList);
			slotIndex++;
		}
	}
	
	private void addGenericEntityRequirementSlot(HttpServletRequest request, EntityPool pool, String categoryName, int slotIndex, String entityFieldName, List<Key> genericRequirementSlotList)
	{
		if (categoryName==null) throw new IllegalArgumentException("categoryName cannot be null.");
		
		@SuppressWarnings("unchecked")
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
		@SuppressWarnings("unchecked")
		List<Map<String,String>> formattedEntityRequirements = (List<Map<String,String>>)formattedRequirementsCategory.get("list");

		Map<String, String> fer = new HashMap<String, String>();	// fer = Formatted Entity Requirement
		
		// Formatting the output
		StringBuilder name = new StringBuilder();
		StringBuilder description = new StringBuilder();
		StringBuilder gerKeyStringList = new StringBuilder();
		boolean firstTime = true;
		for(Key gerKey:genericRequirementSlotList)
		{
			if (firstTime)
				firstTime = false;
			else
			{
				name.append(" or ");
				description.append("<hr class='hr-or'>");
				gerKeyStringList.append(",");
			}
			
			CachedEntity ger = pool.get(gerKey);
			name.append(ger.getProperty("name"));
			description.append(ger.getProperty("description"));
			gerKeyStringList.append(KeyFactory.keyToString(gerKey));
		}
		fer.put("name", name.toString());
		fer.put("description", description.toString());
		fer.put("slotName", entityFieldName+":"+slotIndex);
		fer.put("gerKeyList", gerKeyStringList.toString());
		
		
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
		CachedEntity ideaDef = db.getEntity((Key)idea.getProperty("_definitionKey"));
		
		// Make sure the idea we're processing is actually owned by the character who is executing it
		if (GameUtils.equals(idea.getProperty("characterKey"), character.getKey())==false)
			throw new IllegalArgumentException("Possible hack attempt. An ideaId from a different character was used.");
		
		// Load all the generic entity requirements (but not the subentities. That's why I didn't use inventionservice.poolConstructItemIdea())
		pool.addToQueue(ideaDef.getProperty("skillMaterialsRequired"));
		pool.addToQueue(ideaDef.getProperty("prototypeItemsConsumed"));
		pool.addToQueue(ideaDef.getProperty("skillMaterialsOptional"));
		pool.addToQueue(ideaDef.getProperty("skillToolsRequired"));
		pool.addToQueue(ideaDef.getProperty("prototypeItemsRequired"));
		pool.addToQueue(ideaDef.getProperty("skillToolsOptional"));

		pool.loadEntities();
		
		addGenericEntityRequirements(request, db, pool, "Required Materials", ideaDef, "skillMaterialsRequired");
		addGenericEntityRequirements(request, db, pool, "Required Materials", ideaDef, "prototypeItemsConsumed");
		addGenericEntityRequirements(request, db, pool, "Optional Materials", ideaDef, "skillMaterialsOptional");
		addGenericEntityRequirements(request, db, pool, "Required Tools/Equipment", ideaDef, "skillToolsRequired");
		addGenericEntityRequirements(request, db, pool, "Required Tools/Equipment", ideaDef, "prototypeItemsRequired");
		addGenericEntityRequirements(request, db, pool, "Optional Tools/Equipment", ideaDef, "skillToolsOptional");
		
		request.setAttribute("ideaId", ideaId);
		request.setAttribute("ideaName", idea.getProperty("name"));
	}
	
	
	private void processForConstructItemSkill(HttpServletRequest request, ODPDBAccess db)
	{
		CachedEntity character = db.getCurrentCharacter(); 
		CachedDatastoreService ds = db.getDB();
		EntityPool pool = new EntityPool(ds);
		Long skillId = Long.parseLong(request.getParameter("constructItemSkillId"));
		CachedEntity skill = db.getEntity(KeyFactory.createKey("ConstructItemSkill", skillId));
		
		// Make sure the idea we're processing is actually owned by the character who is executing it
		if (GameUtils.equals(skill.getProperty("characterKey"), character.getKey())==false)
			throw new IllegalArgumentException("Possible hack attempt. A skillId from a different character was used.");
		
		// Load all the generic entity requirements (but not the subentities. That's why I didn't use inventionservice.poolConstructItemIdea())
		pool.addToQueue(skill.getProperty("_definitionKey"));
		pool.loadEntities();
		
		CachedEntity ideaDef = pool.get((Key)skill.getProperty("_definitionKey"));
		pool.addToQueue(ideaDef.getProperty("skillMaterialsRequired"));
		pool.addToQueue(ideaDef.getProperty("skillMaterialsOptional"));
		pool.addToQueue(ideaDef.getProperty("skillToolsRequired"));
		pool.addToQueue(ideaDef.getProperty("skillToolsOptional"));

		pool.loadEntities();
		
		addGenericEntityRequirements(request, db, pool, "Required Materials", ideaDef, "skillMaterialsRequired");
		addGenericEntityRequirements(request, db, pool, "Optional Materials", ideaDef, "skillMaterialsOptional");
		addGenericEntityRequirements(request, db, pool, "Required Tools/Equipment", ideaDef, "skillToolsRequired");
		addGenericEntityRequirements(request, db, pool, "Optional Tools/Equipment", ideaDef, "skillToolsOptional");
		
		request.setAttribute("skillId", skillId);
		request.setAttribute("skillName", skill.getProperty("name"));
	}
	
	
	
	
	private void processForGeneric(HttpServletRequest request, ODPDBAccess db)
	{
		CachedDatastoreService ds = db.getDB();
		EntityPool pool = new EntityPool(ds);
		Key entityKey = KeyFactory.stringToKey(request.getParameter("entity"));
		CachedEntity entity = db.getEntity(entityKey);
		String[] entityFields = request.getParameter("entityFields").split(",");
		
		// Load all the generic entity requirements (but not the subentities. That's why I didn't use inventionservice.poolConstructItemIdea())
		for(String entityField:entityFields)
			pool.addToQueue(entity.getProperty(entityField));

		pool.loadEntities();

		for(String entityField:entityFields)
			addGenericEntityRequirements(request, db, pool, "", entity, entityField);
		
		request.setAttribute("entity", request.getParameter("entity"));
		request.setAttribute("processName", request.getParameter("processName"));
		
	}

	@SuppressWarnings("unchecked")
	private void processForGenericCommand(HttpServletRequest request, ODPDBAccess db)
	{
		processForGeneric(request, db);
		
		JSONObject params = new JSONObject();
		for(String key:((Map<String,String[]>)request.getParameterMap()).keySet())
		{
			params.put(key, request.getParameter(key));
			request.setAttribute(key, request.getParameter(key));
		}
		
		request.setAttribute("commandName", request.getParameter("cmd"));
		request.setAttribute("commandParameters", params.toJSONString());
	}

	private void processForGenericLongOperation(HttpServletRequest request, ODPDBAccess db)
	{
		
		processForGeneric(request, db);
		
		//TODO: THIS
		JSONObject params = new JSONObject();
		params.putAll(request.getParameterMap());
		
		request.setAttribute("commandName", request.getParameter("cmd"));
		request.setAttribute("commandParameters", params.toJSONString());
	}
	
	
	
	
//	private List<Key> rawKeysToKeys(String[] rawKeys)
//	{
//		List<Key> result = new ArrayList<Key>();
//		for(String rawKey:rawKeys)
//		{
//			if (rawKey.endsWith("\")"))
//			{
//				// Keys using names
//				String[] parts = rawKey.split("\\(\"");
//				if (parts.length!=2)
//					throw new IllegalArgumentException("Key was malformed: "+rawKey);
//				
//				String kind = parts[0];
//				String name = parts[1].substring(0, parts[1].length()-2);
//				
//				result.add(KeyFactory.createKey(kind, name));
//			}
//			else
//			{
//				// Keys using IDs
//				String[] parts = rawKey.split("\\(");
//				if (parts.length!=2)
//					throw new IllegalArgumentException("Key was malformed: "+rawKey);
//				
//				String kind = parts[0];
//				String idStr = parts[1].substring(0, parts[1].length()-1);
//				Long id = Long.parseLong(idStr);
//				
//				result.add(KeyFactory.createKey(kind, id));
//			}
//		}
//		return result;
//	}
	
	private Type determineType(HttpServletRequest request)
	{
		if (request.getParameterMap().containsKey("ideaId"))
			return Type.IdeaToPrototype;
		else if (request.getParameterMap().containsKey("constructItemSkillId"))
			return Type.ConstructItemSkill;
		else if (request.getParameterMap().containsKey("collectableId"))
			return Type.CollectCollectable;
		else if (request.getParameterMap().containsKey("entity"))
		{
			if (request.getParameter("cmd")!=null)
				return Type.GenericCommand;
			else
				return Type.GenericLongOperation;
		}
		
		throw new IllegalArgumentException("Unable to determine type for confirm requirements page.");
	}
}

