package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.InitiumPageController;
import com.universeprojects.miniup.server.NotLoggedInException;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

@Controller
public class SublocationsController extends PageController {
	
	public enum PathType
	{
		Permanent,PlayerHouse,CampSite,BlockadeSite,CollectionSite,CombatSite
	}
	
	public SublocationsController() {
		super("sublocations");
	}
	
	@Override
	protected final String processRequest(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException 
	{
		ODPDBAccess db = ODPDBAccess.getInstance(request);
		try{InitiumPageController.requireLoggedIn(db);}catch(NotLoggedInException e){return InitiumPageController.loginMessagePage;}
		
		
	    CachedEntity character = db.getCurrentCharacter();
	    
	    if(CommonChecks.checkCharacterIsIncapacitated(character)) {
	    	request.setAttribute("locationsViewable", false);
	    	return "/WEB-INF/odppages/ajax_sublocations.jsp";
	    }
	    request.setAttribute("locationsViewable", true);
		
		boolean showHidden = "true".equals(request.getParameter("showHidden"));
		
		request.setAttribute("showHidden", showHidden);
		
	    

	    
	    Key locationKey = (Key)character.getProperty("locationKey");
	    db.pool.addToQueue(locationKey);
	    
	    List<CachedEntity> paths = db.getVisiblePathsByLocation(character.getKey(), db.getCharacterLocationKey(character), showHidden);
	    
	    CachedEntity currentLocation = db.pool.get(locationKey);
	    
	    
	    // First sort paths by type a little bit
	    Collections.sort(paths, new Comparator<CachedEntity>()
	    {
	    	
			@Override
			public int compare(CachedEntity o1, CachedEntity o2)
			{
				int o1Type = 0;
				try
				{
					o1Type = PathType.valueOf((String)o1.getProperty("type")).ordinal();
				}
				catch(Exception e)
				{}
				
				int o2Type = 0;
				try
				{
					o2Type = PathType.valueOf((String)o2.getProperty("type")).ordinal();
				}
				catch(Exception e)
				{}
				
				return o1Type-o2Type;
			}
	    	
	    });

		for(int i = paths.size()-1; i>=0; i--)
	    {
			CachedEntity path = paths.get(i);
			if (path==null) paths.remove(i);
			
	    	Key location1Key = (Key)path.getProperty("location1Key");
	    	Key location2Key = (Key)path.getProperty("location2Key");
	    	
			if (GameUtils.equals(location1Key, locationKey)==false && "FromLocation1Only".equals(path.getProperty("forceOneWay")))
				paths.remove(i);
			else if (GameUtils.equals(location2Key, locationKey)==false && "FromLocation2Only".equals(path.getProperty("forceOneWay")))
				paths.remove(i);    	
	    }
	    
	    Set<Key> forgettableLocations = new HashSet<>();
	    List<Key> locationKeys = new ArrayList<>();
	    int forgettableCombatSites = 0;
		StringBuilder forgettableCombatSiteList = new StringBuilder();
		forgettableCombatSiteList.append("\"");
		for(CachedEntity path:paths)
	    {
	    	Key location1Key = (Key)path.getProperty("location1Key");
	    	Key location2Key = (Key)path.getProperty("location2Key");
	    	
	    	Key destLocationKey = null;
	    	if (GameUtils.equals(location1Key, locationKey))
	    		destLocationKey = location2Key;
	    	else
	    		destLocationKey = location1Key;
	    	
	    	locationKeys.add(destLocationKey);
	    	
	    	CachedEntity location = db.pool.get(destLocationKey);
	    	
	    	if (CommonChecks.checkLocationIsCombatSite(location) || CommonChecks.checkLocationIsCampSite(location))
	    	{
	    		forgettableLocations.add(location.getKey());
				forgettableCombatSites++;
				String destLocationKeyId = String.valueOf(destLocationKey.getId());
				forgettableCombatSiteList.append(destLocationKeyId+",");
	    	}
	    	
	    }
		if(forgettableCombatSites > 1) {
			//remove the last comma
			forgettableCombatSiteList.deleteCharAt(forgettableCombatSiteList.length()-1);
			forgettableCombatSiteList.append("\"");
			request.setAttribute("forgetAllCombatSitesHtml", "<p class='center'><a onclick='doForgetAllCombatSites(event, "+forgettableCombatSiteList.toString()+")'>Forget all forgettable sites</a></p>");

		}

	    
	    List<CachedEntity> locations = db.pool.get(locationKeys);
	    
	    List<Map<String,String>> locationsFormatted = new ArrayList<>();
	    
	    for(int i = 0; i<locations.size(); i++)
	    {
	    	CachedEntity location = locations.get(i);
	    	CachedEntity path = paths.get(i);
	    	
	    	if (location==null || (GameUtils.equals(currentLocation.getProperty("mapComponentType"), "Global") && GameUtils.equals(location.getProperty("mapComponentType"), "Global")))
	    		continue;
	    	
	    	
	    	Map<String,String> data = new HashMap<>();
	    	if (forgettableLocations.contains(location.getKey()))
	    		data.put("isForgettable", "true");
	    	data.put("pathId", path.getId().toString());
	    	data.put("locationId", location.getId().toString());
	    	data.put("name", (String)location.getProperty("name"));
	    	data.put("bannerUrl", GameUtils.getResourceUrl((String)location.getProperty("banner")));
	    	
	    	locationsFormatted.add(data);
	    }
		
	    request.setAttribute("locations", locationsFormatted);

	    
	    
	    return "/WEB-INF/odppages/ajax_sublocations.jsp";
	}
	
	
}
