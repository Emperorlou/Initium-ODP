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
import com.universeprojects.miniup.server.GameUtils;
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
		ODPDBAccess db = ODPDBAccess.getInstance(request);
		ODPKnowledgeService knowledge = db.getKnowledgeService(db.getCurrentCharacter().getKey());
		ODPInventionService invention = db.getInventionService(db.getCurrentCharacter(), knowledge);
	
		populateKnowledgePageData(request, db, invention);
		populateExperimentPageData(request, db, invention);
		populateIdeaPageData(request, db, invention);
		
		
		return "/WEB-INF/odppages/ajax_invention.jsp";
	}
	
	@SuppressWarnings("unchecked")
	private void populateKnowledgePageData(HttpServletRequest request, ODPDBAccess db, ODPInventionService invention)
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
	
	private void populateIdeaPageData(HttpServletRequest request, ODPDBAccess db, ODPInventionService invention)
	{
		List<CachedEntity> constructItemIdeas = invention.getAllItemConstructionIdeas();
		Map<Key, CachedEntity> itemDefs = invention.getAllItemDefsForItemConstructionIdeas();
		
		List<Map<String, Object>> ideas = new ArrayList<Map<String, Object>>();
		for(CachedEntity idea:constructItemIdeas)
		{
			String ideaName = (String)idea.getProperty("name");
			Long ideaId = idea.getId();
			CachedEntity itemDef = itemDefs.get((Key)idea.getProperty("itemDef"));
			if (itemDef==null) continue;	// This shouldn't happen really, but if it does we'll skip.
			String iconUrl = GameUtils.getResourceUrl(itemDef.getProperty("icon"));
			
			Map<String, Object> ideaData = new HashMap<String, Object>();
			
			ideaData.put("name", ideaName);
			ideaData.put("id", ideaId);
			ideaData.put("icon", iconUrl);
			
			ideas.add(ideaData);
		}
		request.setAttribute("ideas", ideas);
		boolean hasIdeas = true;
		if (ideas.isEmpty())
			hasIdeas = false;
		request.setAttribute("hasIdeas", hasIdeas);
	}
	
	private void populateExperimentPageData(HttpServletRequest request, ODPDBAccess db, ODPInventionService invention)
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