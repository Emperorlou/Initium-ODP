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
import com.universeprojects.cacheddatastore.QueryHelper;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.InitiumPageController;
import com.universeprojects.miniup.server.NotLoggedInException;
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
		try{InitiumPageController.requireLoggedIn(db);}catch(NotLoggedInException e){return InitiumPageController.loginMessagePage;}

		CachedEntity character = db.getCurrentCharacter();
		
		Key locationKey = db.getCharacterLocationKey(character);
		if(locationKey == null)
			throw new RuntimeException("Invalid game state. Character location null");
		
		if("Character".equals(locationKey.getKind()) ||
				CommonChecks.checkCharacterIsInCombat(character) ||
				GameUtils.isPlayerIncapacitated(character))
			return null;

		// Get all the items we see here in this location...
		List<CachedEntity> collectablesHere = null;
		QueryHelper query = new QueryHelper(db.getDB());
		collectablesHere = query.getFilteredList("Collectable", 50, null, "locationKey", FilterOperator.EQUAL, locationKey);
		
		StringBuilder collectablesHtml = new StringBuilder();
		if (collectablesHere!=null && collectablesHere.isEmpty()==false)
		{
			collectablesHtml.append("<h5>Resources</h5>");
			
			for(CachedEntity collectable:collectablesHere)
			{
				Long secondsTime = (Long)collectable.getProperty("extractionEase");
				if (secondsTime==null)
					secondsTime = 0L;
				
				collectablesHtml.append("<div class='main-item'> ");
				collectablesHtml.append("<div class='main-item-container'>");
				collectablesHtml.append(GameUtils.renderCollectable(collectable)); 
				collectablesHtml.append("<br>");
				collectablesHtml.append("<div class='main-item-controls'>");
				collectablesHtml.append("<a href='#' onclick='doCollectCollectable(event, "+collectable.getKey().getId()+")'>Extract/Collect</a>");
				collectablesHtml.append("</div>"); 
				collectablesHtml.append("</div>");
				collectablesHtml.append("</div>");
				collectablesHtml.append("<br/>");
			}
		}
	
	    request.setAttribute("collectablesToShow", collectablesHtml.toString());
		
		
		
		
		
		
		List<CachedEntity> itemsHere = db.getItemsListSortedForLocation(null, locationKey);
	    int itemsCount = itemsHere.size();
	    itemsCount+=collectablesHere.size();
	    
	    request.setAttribute("itemsCount", itemsHere.size() < 50 ? itemsCount : itemsCount+"+");
	    
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
            	sb.append("<a onclick='pagePopup(\"/odp/ajax_moveitems?selfSide=Character_"+character.getId()+"&otherSide=Item_"+item.getId()+"\")'>Open</a>");
            if (item.getProperty("dogecoins")!=null)
            	sb.append("<a shortcut='71' onclick='collectDogecoinsFromItem("+item.getId()+", event, false)'>Collect "+item.getProperty("dogecoins")+" gold</a>");
            sb.append("<a onclick='doCollectItem(event, "+item.getId()+")'>Collect</a>");
            
            sb.append("</div>"); 
            sb.append("</div>");
            sb.append("</div>");
            sb.append("<br/>");
            
            itemsToShow.add(sb.toString());
	    }
	    if(itemsToShow.isEmpty()==false)
	    	itemsToShow.add(0, "<h5>Items</h5>");
	    
	    request.setAttribute("itemsToShow", itemsToShow);
	    request.setAttribute("isMaxItems", itemsHere.size()==50);
	    
		return "/WEB-INF/odppages/locationitemlist.jsp";
	}

}
