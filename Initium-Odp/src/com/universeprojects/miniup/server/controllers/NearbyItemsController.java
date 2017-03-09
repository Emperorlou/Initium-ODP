package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

@Controller
public class NearbyItemsController extends PageController 
{
	public NearbyItemsController() 
	{
		super("locationitemlist");
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
		
		if(CommonChecks.checkCharacterIsInCombat(character))
			return null;
		
		List<CachedEntity> itemsHere = db.getItemsListSortedForLocation(null, locationKey);
	    int itemsCount = itemsHere.size();
	    
	    request.setAttribute("itemsCount", itemsCount < 50 ? itemsCount : "50+");
	    
	    List<String> itemsToShow = new ArrayList<String>();
	    for(CachedEntity item:itemsHere)
	    {
	    	StringBuilder sb = new StringBuilder();
	    	sb.append("<div class='main-item'> ");
	    	sb.append("<div class='main-item-container'>");
	    	sb.append(GameUtils.renderItem(item));
	    	sb.append("<br>");
	    	sb.append("<div class='main-item-controls'>");
            if (item.getProperty("maxWeight")!=null)
            	sb.append("<a onclick='pagePopup(\"ajax_moveitems.jsp?selfSide=Character_"+character.getId()+"&otherSide=Item_"+item.getId()+"\")'>Open</a>");
            sb.append("<a onclick='ajaxAction(\"ServletCharacterControl?type=collectItem&itemId="+item.getId()+"\", event, function(){})'>Collect</a>");
    
            sb.append("</div>"); 
            sb.append("</div>");
            sb.append("</div>");
            sb.append("<br/>");
            
            itemsToShow.add(sb.toString());
	    }
	    if(itemsToShow.isEmpty()==false)
	    	itemsToShow.add(0, "<h5>Items</h5>");
	    
	    request.setAttribute("itemsToShow", itemsToShow);
	    
		return "/WEB-INF/odppages/locationitemlist.jsp";
	}

}
