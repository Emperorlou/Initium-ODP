package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.miniup.server.InitiumPageController;
import com.universeprojects.miniup.server.NotLoggedInException;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.dbentities.QuestDefEntity;
import com.universeprojects.miniup.server.dbentities.QuestDefEntity.Objective;
import com.universeprojects.miniup.server.dbentities.QuestEntity;
import com.universeprojects.miniup.server.services.QuestService;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

@Controller
public class QuestController extends PageController {
	
	public QuestController() {
		super("quest");
	}

	@Override
	protected final String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		ODPDBAccess db = ODPDBAccess.getInstance(request);
		try{InitiumPageController.requireLoggedIn(db);}catch(NotLoggedInException e){return InitiumPageController.loginMessagePage;}

		QuestService questService = new QuestService(db);
		
		String questDefKeyString = request.getParameter("key");
		Key questDefKey = KeyFactory.stringToKey(questDefKeyString);
		QuestDefEntity questDef = new QuestDefEntity(db, db.getDB().getIfExists(questDefKey));
		QuestEntity quest = questDef.getQuestEntity(db.getCurrentCharacterKey());
		
		if (quest==null)
		{
			return null;
		}
		
		String description = questDef.getDescription();
		
		description+="<h2>Objectives</h2>";
		
		boolean questComplete = true;
		List<Objective> objectives = questDef.getObjectives();
		if (objectives==null || objectives.isEmpty())
			description+="None specified.";
		else
		{
			description+="<ul>";
			for(Objective objective:objectives)
			{
				if (objective.complete || quest.isComplete())
					description+="<li style='color:#c7a46c'><span style='font-size:120%'>&#10004;</span> "+objective.name+"</li>";
				else
					description+="<li>"+objective.name+"</li>";
				
				if (objective.complete==false && quest.isComplete()==false) questComplete = false;
			}
			description+="</ul>";
		}
		
		
		// If the quest is now complete, lets save this new status
		boolean newQuestGiven = questService.updateQuest(quest, questDef, questComplete);
		
		if (newQuestGiven)
		{
			// We need to refresh the page popup that shows quests so the player can see he has another quest to do after this...
			description+="<script type='text/javascript'>reloadPagePopup();</script>";
		}
		
		request.setAttribute("questComplete", questComplete);
		request.setAttribute("name", questDef.getName());
		request.setAttribute("description", description);
		request.setAttribute("questDefKey", questDef.getUrlSafeKey());
		
		
		
		return "/WEB-INF/odppages/ajax_quest.jsp";
	}
	
}