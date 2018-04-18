package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.EntityPool;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.InitiumPageController;
import com.universeprojects.miniup.server.NotLoggedInException;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.services.ODPInventionService;
import com.universeprojects.miniup.server.services.ODPKnowledgeService;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

@Controller
public class InventionController extends PageController {
	
	public InventionController() {
		super("invention");
	}

	@Override
	protected final String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
//		if (GameUtils.isTestServer(request))
//		{
			ODPDBAccess db = ODPDBAccess.getInstance(request);
			try{InitiumPageController.requireLoggedIn(db);}catch(NotLoggedInException e){return InitiumPageController.loginMessagePage;}

			
			EntityPool pool = new EntityPool(db.getDB());
			ODPKnowledgeService knowledge = db.getKnowledgeService(db.getCurrentCharacter().getKey());
			ODPInventionService invention = db.getInventionService(db.getCurrentCharacter(), knowledge);
		
			populateKnowledgePageData(request, db, invention, pool);
			populateExperimentPageData(request, db, invention, pool);
			populateIdeaPageData(request, db, invention, pool);
			populateConstructItemSkillPageData(request, db, invention, pool);
//		}
		
		
		return "/WEB-INF/odppages/ajax_invention.jsp";
	}
	
	@SuppressWarnings("unchecked")
	private void populateKnowledgePageData(HttpServletRequest request, ODPDBAccess db, ODPInventionService invention, EntityPool pool)
	{
		List<CachedEntity> allKnowledgeEntities = invention.getKnowledgeService().getAllKnowledge();
		Map<String, Object> knowledgeTree = processKnowledgeTreeRecursive(null, allKnowledgeEntities);
		request.setAttribute("knowledgeTree", knowledgeTree);
		boolean hasKnowledge = true;
		if (((List<Object>)knowledgeTree.get("children")).isEmpty())
			hasKnowledge = false;
		request.setAttribute("hasKnowledge", hasKnowledge);
	}
	
	/**
	 * This processes the knowledge entities
	 * @param currentNode
	 * @param allKnowledgeEntities
	 * @return
	 */
	private Map<String,Object> processKnowledgeTreeRecursive(CachedEntity currentNode, List<CachedEntity> allKnowledgeEntities)
	{
		Map<String,Object> result = new HashMap<String,Object>();
		
		// Populate the current node data
		if (currentNode!=null)
		{
			result.put("name", currentNode.getProperty("name"));
			result.put("id", currentNode.getId());
			result.put("iconUrl", currentNode.getProperty("icon"));
			result.put("experience", GameUtils.formatNumber(currentNode.getProperty("experience")));
			
		}

		List<Map<String,Object>> children = new ArrayList<Map<String,Object>>();
		for(CachedEntity knowledge:allKnowledgeEntities)
			if ((currentNode==null && knowledge.getProperty("parentKnowledge")==null) || 
					currentNode.getKey().equals(knowledge.getProperty("parentKnowledge")))
				children.add(processKnowledgeTreeRecursive(knowledge, allKnowledgeEntities));

		result.put("children", children);
		
		return result;
	}
	
	private void poolIdeaPageData(EntityPool pool, List<CachedEntity> constructItemIdeas)
	{
		// Level 1
		for(CachedEntity idea:constructItemIdeas)
		{
			pool.addToQueue(idea.getProperty("_definitionKey"));
		}
		
		pool.loadEntities();
		
		// Level 2
		for(CachedEntity idea:constructItemIdeas)
		{
			CachedEntity ideaDef = pool.get((Key)idea.getProperty("_definitionKey"));
			if (ideaDef==null) continue;
			pool.addToQueue(ideaDef.getProperty("itemDef"));
		}	
		
		pool.loadEntities();
	}
	
	private void populateIdeaPageData(HttpServletRequest request, ODPDBAccess db, ODPInventionService invention, EntityPool pool)
	{
		List<CachedEntity> constructItemIdeas = invention.getAllItemConstructionIdeas();
		poolIdeaPageData(pool, constructItemIdeas);
		
		List<Map<String, Object>> ideas = new ArrayList<Map<String, Object>>();
		for(CachedEntity idea:constructItemIdeas)
		{
			CachedEntity ideaDef = pool.get((Key)idea.getProperty("_definitionKey"));
			
			if (ideaDef==null) continue;
			
			String ideaName = (String)ideaDef.getProperty("name");
			String ideaDescription = (String)ideaDef.getProperty("ideaDescription");
			Long ideaId = idea.getId();
			CachedEntity itemDef = pool.get((Key)ideaDef.getProperty("itemDef"));
			String iconUrl = null;
			if (itemDef!=null)
				iconUrl = GameUtils.getResourceUrl(itemDef.getProperty("icon"));
			Long ideaSpeed = (Long)ideaDef.getProperty("prototypeConstructionSpeed");
			
			Map<String, Object> ideaData = new HashMap<String, Object>();
			
			ideaData.put("name", ideaName);
			ideaData.put("id", ideaId);
			ideaData.put("icon", iconUrl);
			ideaData.put("description", ideaDescription);
			ideaData.put("speed", ideaSpeed);
			
			ideas.add(ideaData);
		}
		request.setAttribute("ideas", ideas);
		boolean hasIdeas = true;
		if (ideas.isEmpty())
			hasIdeas = false;
		request.setAttribute("hasIdeas", hasIdeas);
	}
	
	private void poolSkillPageData(EntityPool pool, List<CachedEntity> constructItemSkills)
	{
		// Level 1
		for(CachedEntity skill:constructItemSkills)
		{
			pool.addToQueue(skill.getProperty("_definitionKey"));
			pool.addToQueue(skill.getProperty("item"));
		}
		
		pool.loadEntities();
		
	}
	
	
	private void populateConstructItemSkillPageData(HttpServletRequest request, ODPDBAccess db, ODPInventionService invention, EntityPool pool)
	{
		List<CachedEntity> constructItemSkills = invention.getAllItemConstructionSkills();
		poolSkillPageData(pool, constructItemSkills);
		
		List<Map<String, Object>> skills = new ArrayList<Map<String, Object>>();
		for(CachedEntity skill:constructItemSkills)
		{
			String skillName = (String)skill.getProperty("name");
			String skillDescription = (String)skill.getProperty("skillDescription");
			Long skillSpeed = (Long)skill.getProperty("skillConstructionSpeed");
			Long skillId = skill.getId();
			CachedEntity item = null;
			String iconUrl = null;
			if (skill.getProperty("item")!=null)
			{
				item = pool.get((Key)skill.getProperty("item"));
				iconUrl = GameUtils.getResourceUrl(item.getProperty("icon"));
			}
			
			// We'll also get the idea this skill came from
			CachedEntity idea = pool.get((Key)skill.getProperty("_definitionKey"));
			
			if (idea==null)
				continue;
			
			Map<String, Object> skillData = new HashMap<String, Object>();
			
			skillData.put("name", skillName);
			skillData.put("description", skillDescription);
			skillData.put("speed", skillSpeed);
			skillData.put("id", skillId);
			skillData.put("icon", iconUrl);
			skillData.put("class", idea.getProperty("name"));
			
			skills.add(skillData);
		}
		request.setAttribute("constructItemSkills", skills);
		boolean hasSkills = true;
		if (skills.isEmpty())
			hasSkills = false;
		request.setAttribute("hasConstructItemSkills", hasSkills);
	}
	
	private void populateExperimentPageData(HttpServletRequest request, ODPDBAccess db, ODPInventionService invention, EntityPool pool)
	{
		// Get all available items (but only unique by name) that you have access to...
		List<CachedEntity> allAvailableItems = invention.getAvailableItems();
		Map<String, CachedEntity> allAvailableUniqueItems = new HashMap<String, CachedEntity>();
		for(CachedEntity e:allAvailableItems)
			allAvailableUniqueItems.put((String)e.getProperty("name"), e);
		Collection<CachedEntity> availableItems = allAvailableUniqueItems.values();
		List<Map<String, Object>> availableItemsData = new ArrayList<Map<String, Object>>();
		for(CachedEntity item:availableItems)
		{
			Map<String,Object> availableItemData = new HashMap<String,Object>();
			availableItemData.put("html", GameUtils.renderItem(db, db.getCurrentCharacter(), item));
			availableItemData.put("id", item.getId());
			
			availableItemsData.add(availableItemData);
		}
		request.setAttribute("availableItems", availableItemsData);
		boolean hasExperimentItems = true;
		if (availableItemsData.isEmpty())
			hasExperimentItems = false;
		request.setAttribute("hasExperimentItems", hasExperimentItems);
	}
}