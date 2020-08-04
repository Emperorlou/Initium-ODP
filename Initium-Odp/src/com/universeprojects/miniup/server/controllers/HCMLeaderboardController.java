package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.QueryHelper;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.InitiumPageController;
import com.universeprojects.miniup.server.NotLoggedInException;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

@Controller
public class HCMLeaderboardController extends PageController
{

	public HCMLeaderboardController()
	{
		super("hcmleaderboard");
	}

	@Override
	protected String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		ODPDBAccess db = ODPDBAccess.getInstance(request);
		try{InitiumPageController.requireLoggedIn(db);}catch(NotLoggedInException e){return InitiumPageController.loginMessagePage;}
		CachedDatastoreService ds = db.getDB();
		QueryHelper query = new QueryHelper(ds);
		
		List<CachedEntity> characters = query.getFilteredList_Sorted("Character", 50, null, "hardcoreRank", false);
		
		// First go through all the characters and pool their users
		for(CachedEntity c:characters)
			db.pool.addToQueue(c.getProperty("userKey"));
		db.pool.loadEntities();
		
		List<Map<String,String>> charactersFormatted = new ArrayList<>();
		
		for(int i = 0; i<characters.size(); i++)
		{
			CachedEntity character = characters.get(i);
			
			if (CommonChecks.checkIsHardcore(character)&&CommonChecks.checkCharacterIsIncapacitated(character) && GameUtils.getElapsedDays((Date)character.getProperty("locationEntryDatetime"))>1)
				continue;
			
			Map<String,String> data = new HashMap<>();
			data.put("html", GameUtils.renderCharacter(db.pool.get(character.getProperty("userKey")), character, true, false));
			data.put("points", GameUtils.formatNumber(character.getProperty("hardcoreRank")));
			charactersFormatted.add(data);
		}
		
		request.setAttribute("characters", charactersFormatted);
		
	    return "/WEB-INF/odppages/ajax_hcmleaderboard.jsp";
	}

}
