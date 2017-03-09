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
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.CommandAttack;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

@Controller
public class NearbyMerchantsController extends PageController 
{
	public NearbyMerchantsController() 
	{
		super("locationmerchantlist");
	}

	@Override
	protected String processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException 
	{
		ODPDBAccess db = ODPDBAccess.getInstance(request);
		CachedEntity character = db.getCurrentCharacter();
		
		Key locationKey = (Key)character.getProperty("locationKey");
		if(locationKey == null)
			throw new RuntimeException("Invalid game state. Character location null");
		
		List<CachedEntity> shopsHere = db.getCharacterMerchants(null, locationKey);
	    int shopCount = shopsHere.size();
	    
	    request.setAttribute("shopCount", shopCount);
	    
	    List<String> shopsToShow = new ArrayList<String>();
	    for(CachedEntity shop:shopsHere)
	    {
	    	if (((Double)shop.getProperty("hitpoints"))<=0)
                continue;
	    	
	    	StringBuilder sb = new StringBuilder();
            String storeStyleCustomization = "";
            if (shop.getProperty("storeStyleCustomization")!=null)
                storeStyleCustomization = (String)shop.getProperty("storeStyleCustomization");
            
            sb.append("<div>");
            sb.append("<div class='main-merchant-container'>");
            sb.append("<a onclick='viewStore("+shop.getKey().getId()+")'>");
            sb.append("<div class='main-item'>"+shop.getProperty("name")+" - ");
            sb.append("<div class='main-item-subnote' style='"+storeStyleCustomization+"'>"+shop.getProperty("storeName"));
            sb.append("</div></a></div></div></div>");
            
            shopsToShow.add(sb.toString());
	    }
	    
	    request.setAttribute("shopsToShow", shopsToShow);
	    
		return "/WEB-INF/odppages/locationmerchantlist.jsp";
	}

}
