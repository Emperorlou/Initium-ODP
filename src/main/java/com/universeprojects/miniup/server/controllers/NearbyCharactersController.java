package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.InitiumPageController;
import com.universeprojects.miniup.server.NotLoggedInException;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.CommandAttack;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

@Controller
public class NearbyCharactersController extends PageController {

	public NearbyCharactersController() 
	{
		super("locationcharacterlist");
	}

	@Override
	protected String processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		ODPDBAccess db = ODPDBAccess.getInstance(request);
		try{InitiumPageController.requireLoggedIn(db);}catch(NotLoggedInException e){return InitiumPageController.loginMessagePage;}

		CachedEntity character = db.getCurrentCharacter();
		
		Key locationKey = db.getCharacterLocationKey(character);
		if(locationKey == null)
			throw new RuntimeException("Invalid game state. Character location null");
		
		CachedEntity location = db.getEntity(locationKey);
		if(location == null)
			throw new RuntimeException("Null entity for locationKey " + locationKey.getId());
		
		if(CommonChecks.checkCharacterIsInCombat(character))
			return null;
		
		List<CachedEntity> charactersHere = db.getFilteredList("Character", 51, "locationKey", FilterOperator.EQUAL, locationKey);
	    int charCount = charactersHere.size();
	    
	    request.setAttribute("charCount", charCount <= 50 ? charCount-1 : "50+");
	    
	    List<String> charsToShow = new ArrayList<String>();
	    for(CachedEntity curChar:charactersHere)
	    {
	    	if(GameUtils.equals(curChar.getKey(), character.getKey())) continue;
	    	
	    	StringBuilder sb = new StringBuilder();
	    	boolean dead = false;
	    	if (curChar.getProperty("hitpoints")==null) continue;
	    	if (((Double)curChar.getProperty("hitpoints"))<=0)
                dead = true;
	    	
	    	sb.append("<div>");
	    	sb.append("<div class='main-item-container'>");
	    	sb.append("<a class='main-item clue' rel='/odp/viewcharactermini?characterId="+curChar.getId()+"'>"+curChar.getProperty("name"));
	    	sb.append("<div class='main-item-controls' style='top:0px'>");
            if (dead)
            {
            	sb.append("<a shortcut='71' onclick='collectDogecoinsFromCharacter("+curChar.getId()+", event)'>Collect "+curChar.getProperty("dogecoins")+" gold</a> ");
            	sb.append("<a onclick='doCollectCharacter(event, "+curChar.getId()+")'>Pick up</a>");
            }
            
            if (CommandAttack.canAttack(db.getDB(), location, character, curChar))
            {
            	sb.append("<a onclick='doAttack(event, "+curChar.getId()+")'>Attack</a>");
            }
            sb.append("</div>");
            sb.append("</a>");
            sb.append("</div>");
            sb.append("</div>");
            
            charsToShow.add(sb.toString());
	    }
	    
	    request.setAttribute("charToShow", charsToShow);
	    
		return "/WEB-INF/odppages/locationcharacterlist.jsp";
	}

}
