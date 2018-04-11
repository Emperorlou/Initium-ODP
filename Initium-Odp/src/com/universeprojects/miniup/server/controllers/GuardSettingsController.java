package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.EntityPool;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.InitiumPageController;
import com.universeprojects.miniup.server.NotLoggedInException;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.dbentities.GuardSetting;
import com.universeprojects.miniup.server.dbentities.GuardSetting.GuardExclusion;
import com.universeprojects.miniup.server.dbentities.GuardSetting.GuardType;
import com.universeprojects.miniup.server.services.GuardService;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

@Controller
public class GuardSettingsController extends PageController {

	public GuardSettingsController() {
		super("guardsettings");
	}

	@Override
	protected String processRequest(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		ODPDBAccess db = ODPDBAccess.getInstance(request);
		try{InitiumPageController.requireLoggedIn(db);}catch(NotLoggedInException e){return InitiumPageController.loginMessagePage;}

		response.setHeader("Access-Control-Allow-Origin", "*");     // This is absolutely necessary for phonegap to work

	    CachedDatastoreService ds = db.getDB();
	    CachedEntity character = db.getCurrentCharacter(); 
	    GuardService guardService = new GuardService(db);
	    
	    String locationKeyStr = request.getParameter("locationKey");
	    Key locationKey = null;
	    if (locationKeyStr!=null)
	    	locationKey = KeyFactory.stringToKey(locationKeyStr);
	    if (locationKey==null) locationKey = 
	    		(Key)character.getProperty("locationKey");
	    
	    
	    List<GuardSetting> guardSettings = null;
	    if (locationKey==null)
	    	guardSettings = guardService.getGuardSettings(character.getKey());
	    else
	    	guardSettings = guardService.getGuardSettings(character.getKey(), locationKey);
	    
	    for(GuardSetting gs:guardSettings)
	    {
	    	db.pool.addToQueue(gs.getEntityKey());
	    }
	    
	    db.pool.loadEntities();
	    
	    List<Map<String,String>> formattedGuardSettings = new ArrayList<>();
	    for(GuardSetting gs:guardSettings)
	    {
	    	CachedEntity entity = db.pool.get((Key)gs.getEntityKey());
	    	if (entity==null)
	    	{
	    		ds.delete(gs.getKey());
	    		continue;
	    	}
	    	
	    	String finalGuardLine = gs.getFullLine(entity);
    		
    		Map<String,String> setting = new HashMap<>();
    		setting.put("id", gs.getKey().getId()+"");
    		setting.put("key", gs.getUrlSafeKey());
    		setting.put("text", finalGuardLine);
    		setting.put("active", gs.isActive()+"");
    		formattedGuardSettings.add(setting);
	    }
	    
	    List<GuardSetting> allGS = guardService.getAllActiveGuardSettingsForLocation(locationKey);
	    Map<String, Integer> guardCounts = new HashMap<>();
	    for(GuardType type:GuardType.values())
	    	guardCounts.put(type.toString(), 0);
	    
	    for(GuardSetting guard:allGS)
	    {
	    	GuardType type = guard.getSettings();
    		Integer count = guardCounts.get(type.toString());
    		if (count==null) count = 0;
    		count++;
    		guardCounts.put(type.toString(), count);
	    }

	    request.setAttribute("guardCounts", guardCounts);
	    
	    request.setAttribute("hasGuardSettings", formattedGuardSettings.isEmpty()==false);
	    request.setAttribute("guardSettings", formattedGuardSettings);
	    
	    request.setAttribute("locationKey", locationKey.toString());
	    
		return "/WEB-INF/odppages/ajax_guardsettings.jsp";
	}

	
}
