package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.QueryHelper;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.InitiumObject;
import com.universeprojects.miniup.server.InitiumPageController;
import com.universeprojects.miniup.server.NotLoggedInException;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.aspects.AspectPet;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

public class TeachIdeasListController extends PageController{
	
	public TeachIdeasListController() {
		super("teachideaslist");
	}

	@Override
	protected final String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		ODPDBAccess db = ODPDBAccess.getInstance(request);
		try{InitiumPageController.requireLoggedIn(db);}catch(NotLoggedInException e){return InitiumPageController.loginMessagePage;}
		
		try
		{
			QueryHelper query = new QueryHelper(db.getDB());
			
			CachedEntity character = db.getCurrentCharacter();

			List<Map<String, String>> ideasFormatted = new ArrayList<>();
			List<CachedEntity> ideas = query.getFilteredList("ConstructItemIdea", "characterKey", character.getKey());
			
			for(CachedEntity idea : ideas) {
				Map<String,String> data = new HashMap<>();
				data.put("html", null); //how to render idea similar to invention window?
				data.put("id", idea.getId().toString());
				ideasFormatted.add(data);
				
			}
			
			List<Map<String, String>> charactersFormatted = new ArrayList<>();
			List<CachedEntity> characters = query.getFilteredList("Character", "locationKey", character.getProperty("locationKey"), "mode", "LEARNING");
			
			for(CachedEntity target : characters) {
				Map<String, String> data = new HashMap<>();
				data.put("html", GameUtils.renderCharacter(null, target));
				data.put("id", target.getId().toString());
				charactersFormatted.add(data);
			}
			
			request.setAttribute("ideas", ideasFormatted);
			request.setAttribute("characters", charactersFormatted);
			
		}
		catch(UserErrorMessage e)
		{
			request.setAttribute("error", e.getMessage());
		}
		
		return "/WEB-INF/odppages/ajax_teachideaslist.jsp";
	}
	
}
