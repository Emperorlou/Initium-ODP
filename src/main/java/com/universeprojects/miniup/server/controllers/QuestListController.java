package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.miniup.server.InitiumPageController;
import com.universeprojects.miniup.server.NotLoggedInException;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.dbentities.QuestDefEntity;
import com.universeprojects.miniup.server.dbentities.QuestEntity;
import com.universeprojects.miniup.server.dbentities.QuestEntity.QuestStatus;
import com.universeprojects.miniup.server.services.QuestService;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

@Controller
public class QuestListController extends PageController {
	
	public QuestListController() {
		super("questlist");
	}

	@Override
	protected final String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		ODPDBAccess db = ODPDBAccess.getInstance(request);
		try{InitiumPageController.requireLoggedIn(db);}catch(NotLoggedInException e){return InitiumPageController.loginMessagePage;}

		QuestService questService = new QuestService(null, db, db.getCurrentCharacter());
		
		List<QuestEntity> allQuests = questService.getAllQuests();
		Map<Key, QuestDefEntity> allQuestDefs = questService.getMapOfAllQuestDefs();
		
		List<Map<String,Object>> activeQuests = new ArrayList<>();
		List<Map<String,Object>> finishedQuests = new ArrayList<>();
		
		List<Map<String,Object>> data = new ArrayList<>();
		if (allQuests!=null && allQuests.isEmpty()==false)
		{
			
			// Sort the active quests first
			Collections.sort(allQuests, new Comparator<QuestEntity>()
			{
	
				@Override
				public int compare(QuestEntity o1, QuestEntity o2)
				{
					if (o1.getCreatedDate()==null || o2.getCreatedDate()==null) return 0;
					return o1.getCreatedDate().compareTo(o2.getCreatedDate());
				}
			});
			
			for(QuestEntity quest : allQuests) {
				QuestDefEntity questDef = allQuestDefs.get(quest.getQuestDefKey());
				if(questDef == null) continue;
				
				Map<String, Object> questData = new HashMap<>();
				
				questData.put("name", questDef.getName());
				questData.put("description", questDef.getDescription());
				
				questData.put("complete", quest.isComplete());
				questData.put("key", questDef.getUrlSafeKey());
				
				if(quest.isComplete()) finishedQuests.add(questData);
				else activeQuests.add(questData);
				
			}
		}
		
		request.setAttribute("activeQuests", activeQuests);
		request.setAttribute("hasActiveQuests", activeQuests.size() > 0);
		request.setAttribute("finishedQuests", finishedQuests);
		request.setAttribute("hasFinishedQuests", finishedQuests.size() > 0);
		
		return "/WEB-INF/odppages/ajax_questlist.jsp";
	}
	
}