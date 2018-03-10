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
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
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
		throws ServletException, IOException {
		
		boolean showHidden = "true".equals(request.getParameter("showHidden"));
		
	    ODPDBAccess db = ODPDBAccess.getInstance(request);
	    
	    CachedEntity character = db.getCurrentCharacter();
	    Key locationKey = (Key)character.getProperty("locationKey");
	    db.pool.addToQueue(locationKey);
	    
	    List<CachedEntity> discoveriesForCharacterAndLocation = db.getDiscoveriesForCharacterAndLocation(db.getCurrentCharacterKey(), locationKey, showHidden);
	    List<Key> pathKeys = new ArrayList<>();
	    for(CachedEntity discovery:discoveriesForCharacterAndLocation)
	    {
	    	db.pool.addToQueue(discovery.getProperty("location1Key"), discovery.getProperty("location2Key"), discovery.getProperty("entityKey"));
	    	pathKeys.add((Key)discovery.getProperty("entityKey"));
	    }
	    
	    db.pool.loadEntities();
	    
	    CachedEntity currentLocation = db.pool.get(locationKey);
	    
	    List<CachedEntity> paths = db.pool.get(pathKeys);
	    
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
	    
	    List<Key> locationKeys = new ArrayList<>();
	    for(CachedEntity path:paths)
	    {
	    	Key location1Key = (Key)path.getProperty("location1Key");
	    	Key location2Key = (Key)path.getProperty("location2Key");
	    	
	    	if (GameUtils.equals(location1Key, locationKey))
	    		locationKeys.add(location2Key);
	    	else
	    		locationKeys.add(location1Key);
	    }
	    
	    List<CachedEntity> locations = db.pool.get(locationKeys);
	    
	    List<Map<String,String>> locationsFormatted = new ArrayList<>();
	    
	    for(int i = 0; i<locations.size(); i++)
	    {
	    	CachedEntity location = locations.get(i);
	    	CachedEntity path = paths.get(i);
	    	
	    	if (GameUtils.equals(currentLocation.getProperty("mapComponentType"), "Global") && GameUtils.equals(location.getProperty("mapComponentType"), "Global"))
	    		continue;
	    	
	    	Map<String,String> data = new HashMap<>();
	    	data.put("id", path.getId().toString());
	    	data.put("name", (String)location.getProperty("name"));
	    	data.put("bannerUrl", GameUtils.getResourceUrl((String)location.getProperty("banner")));
	    	locationsFormatted.add(data);
	    }
		
	    request.setAttribute("locations", locationsFormatted);
	    
	    return "/WEB-INF/odppages/ajax_sublocations.jsp";
	}
	
	
}
