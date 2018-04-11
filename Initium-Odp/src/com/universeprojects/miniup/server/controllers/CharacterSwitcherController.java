package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.EntityPool;
import com.universeprojects.cacheddatastore.QueryHelper;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.InitiumPageController;
import com.universeprojects.miniup.server.NotLoggedInException;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

@Controller
public class CharacterSwitcherController extends PageController {
	
	public CharacterSwitcherController() {
		super("characterswitcher");
	}

	@Override
	protected final String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		ODPDBAccess db = ODPDBAccess.getInstance(request);
		try{InitiumPageController.requireLoggedIn(db);}catch(NotLoggedInException e){return InitiumPageController.loginMessagePage;}
		
		CachedEntity character = db.getCurrentCharacter();
		CachedEntity user = db.getCurrentUser();

		QueryHelper query = new QueryHelper(db.getDB());
		
		List<CachedEntity> characters = query.getFilteredList("Character", "userKey", user.getKey());
		List<Map<String,Object>> charactersData = new ArrayList<>();
		
		boolean useSimpleMode = db.getUserCharacterSlotCount(user)>24;
		
		EntityPool pool = new EntityPool(db.getDB());
		if (useSimpleMode==false)
			for(CachedEntity c:characters)
			{
		    	List<Key> equipmentKeys = Arrays.asList(
		    			(Key)character.getProperty("equipmentHelmet"),
						(Key)character.getProperty("equipmentChest"),
						(Key)character.getProperty("equipmentLegs"),
						(Key)character.getProperty("equipmentBoots"),
						(Key)character.getProperty("equipmentGloves"),
						(Key)character.getProperty("equipmentLeftHand"),
						(Key)character.getProperty("equipmentRightHand"),
						(Key)character.getProperty("equipmentShirt"));
		    	pool.addToQueue(equipmentKeys);
		
			}
		
		pool.loadEntities();
		Collections.sort(characters, new Comparator<CachedEntity>()
		{
			@Override
			public int compare(CachedEntity o1, CachedEntity o2)
			{
				return ((String)o1.getProperty("name")).compareTo((String)o2.getProperty("name"));
			}
		});
		
		for(CachedEntity c:characters)
		{
			if (c.getProperty("name").toString().startsWith("Dead ")==false && "Zombie".equals(c.getProperty("status"))==false)
			{}
			else
				continue;
			Map<String,Object> data = new HashMap<>();
			charactersData.add(data);
			
			if (useSimpleMode)
				data.put("html", "<div class='character-widget-simple-mode'>"+c.getProperty("name")+"</div>");
			else
				data.put("html", GameUtils.renderCharacterWidget(pool, request, db, c, user, true));
			data.put("id", c.getKey().getId());
			data.put("name", c.getProperty("name"));
			data.put("urlSafeKey", c.getUrlSafeKey());

		}
		
		request.setAttribute("characters", charactersData);
		request.setAttribute("hasPremium", CommonChecks.checkUserIsPremium(user));
		request.setAttribute("currentCharacterId", db.getCurrentCharacterKey().getId());
		
		return "/WEB-INF/odppages/ajax_characterswitcher.jsp";
	}
	
}