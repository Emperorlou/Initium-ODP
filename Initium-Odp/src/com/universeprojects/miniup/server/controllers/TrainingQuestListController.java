package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.InitiumPageController;
import com.universeprojects.miniup.server.NotLoggedInException;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.dbentities.QuestDefEntity;
import com.universeprojects.miniup.server.services.QuestService;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

@Controller
public class TrainingQuestListController extends PageController {
	public TrainingQuestListController() {
		super("trainingquestlist");
	}

	@Override
	protected String processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		ODPDBAccess db = ODPDBAccess.getInstance(request);
		try{InitiumPageController.requireLoggedIn(db);}catch(NotLoggedInException e){return InitiumPageController.loginMessagePage;}

		List<TrainingQuestLine> questLines = new ArrayList<>();

		// Getting started quests
		questLines.add(new TrainingQuestLine(
				5394555692384256L, 
				5701104157589504L, 
				GameUtils.getResourceUrl("images/ui3/quest-banners/quests-getting-started1.jpg")));
		
		// Instances and bosses quests
		questLines.add(new TrainingQuestLine(
				null, 
				null, 
				GameUtils.getResourceUrl("images/ui3/quest-banners/quests-instances-and-bosses1.jpg")));
		
		// Invention/crafting
		questLines.add(new TrainingQuestLine(
				null, 
				null, 
				GameUtils.getResourceUrl("images/ui3/quest-banners/quests-invention-crafting1.jpg")));
		
		// Farming
		questLines.add(new TrainingQuestLine(
				null, 
				null, 
				GameUtils.getResourceUrl("images/ui3/quest-banners/quests-farming1.jpg")));
		
		// Building construction
		questLines.add(new TrainingQuestLine(
				null, 
				null, 
				GameUtils.getResourceUrl("images/ui3/quest-banners/quests-building-construction1.jpg")));
		
		
		List<Map<String,Object>> formattedQuestLines = new ArrayList<>();
		
		QuestService questService = db.getQuestService(null);
		List<QuestDefEntity> allQuestDefs = questService.getAllQuestDefs();
		
		for(TrainingQuestLine questLine:questLines)
		{
			for (QuestDefEntity questDef:allQuestDefs)
			{
				if (GameUtils.equals(questLine.endingQuestDefKey, questDef.getKey()))
				{
					questLine.complete = true;
					break;
				}
			}
			
			Map<String,Object> data = new HashMap<>();
			data.put("bannerUrl", questLine.bannerUrl);
			if (questLine.startingQuestDefKey!=null)
				data.put("questDefId", questLine.startingQuestDefKey.getId());
			if (questLine.complete)
				data.put("completeCssClass", "quest-line-complete");
			else
				data.put("completeCssClass", "");
			formattedQuestLines.add(data);
		}
		
		request.setAttribute("list", formattedQuestLines);
		
		return "/WEB-INF/odppages/trainingquestlist.jsp";
	}

	public class TrainingQuestLine
	{
		String bannerUrl;
		Key startingQuestDefKey;
		Key endingQuestDefKey;
		boolean complete = false;
		
		public TrainingQuestLine(Long startingQuestDefId, Long endingQuestDefId, String bannerUrl)
		{
			if (startingQuestDefId!=null)
				this.startingQuestDefKey = KeyFactory.createKey("QuestDef", startingQuestDefId);
			if (endingQuestDefId!=null)
				this.endingQuestDefKey = KeyFactory.createKey("QuestDef", endingQuestDefId);
			
			this.bannerUrl = bannerUrl;
		}
	}
	
}
