package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.InitiumAspect;
import com.universeprojects.miniup.server.InitiumObject;
import com.universeprojects.miniup.server.InitiumPageController;
import com.universeprojects.miniup.server.ItemAspect;
import com.universeprojects.miniup.server.NotLoggedInException;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.WebUtils;
import com.universeprojects.miniup.server.aspects.AspectGridMapObject;
import com.universeprojects.miniup.server.ItemAspect.ItemPopupEntry;
import com.universeprojects.miniup.server.services.GridMapService;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

@Controller
public class GridMapCellContentsController extends PageController
{

	public GridMapCellContentsController()
	{
		super("gridmapcellcontents");
	}

	@Override
	protected String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		ODPDBAccess db = ODPDBAccess.getInstance(request);
		try{InitiumPageController.requireLoggedIn(db);}catch(NotLoggedInException e){return InitiumPageController.loginMessagePage;}
		
		int tileX = Integer.parseInt(request.getParameter("tileX"));
		int tileY = Integer.parseInt(request.getParameter("tileY"));
		
		CachedEntity location = db.getCharacterLocation(db.getCurrentCharacter());
		
		GridMapService gmService = new GridMapService(db, location);
		
		List<CachedEntity> items = gmService.generateTileItems(tileX, tileY);
		
		List<Map<String,String>> itemsFormatted = new ArrayList<>();
		for(CachedEntity item:items)
		{
			if (item==null) continue;
			
			String gridMapObjectMode = (String)item.getProperty("GridMapObject:mode");
			if (gridMapObjectMode==null) gridMapObjectMode = "Loose";
			String html = GameUtils.renderItem(db, request, db.getCurrentCharacter(), item, false, false, (String)item.getAttribute("proceduralKey"));
			html += "<br>";
			html += "<div class='main-item-controls'>";

			if (gridMapObjectMode.equals("Loose"))
			{
//	            if (item.getProperty("maxWeight")!=null)
//	            	html+="<a onclick='pagePopup(\"/odp/ajax_moveitems.jsp?selfSide=Character_"+character.getId()+"&otherSide=Item_"+item.getId()+"\")'>Open</a>";
	            if (item.getProperty("dogecoins")!=null)
	            	html+="<a shortcut='71' onclick='collectDogecoinsFromItem("+item.getId()+", event, false)'>Collect "+item.getProperty("dogecoins")+" gold</a>";
	            
	            if (CommonChecks.checkItemIsAccessible(item, db.getCurrentCharacter()) && CommonChecks.checkItemIsMovable(item))
	            {
	            	if (GridMapService.isProceduralItem(item) && CommonChecks.checkItemHasAspect(item, AspectGridMapObject.class)==false)
	            		html+="<a onclick='doCommand(event, \"PickablePick\", {proceduralKey:\""+item.getAttribute("proceduralKey")+"\"});'>Collect</a>";
	            	else
	            		html+="<a onclick='doCollectItem(event, "+item.getId()+")'>Collect</a>";
	            }
	            
			}
								
			InitiumObject iObject = new InitiumObject(db, item);
			if (iObject.hasAspects())
			{
				// Go through the aspects on this item and include any special links that it may have
				for(InitiumAspect initiumAspect:iObject.getAspects())
				{
					if (initiumAspect instanceof ItemAspect)
					{
						ItemAspect itemAspect = (ItemAspect)initiumAspect;
						
						List<ItemPopupEntry> curEntries = itemAspect.getItemPopupEntries(db.getCurrentCharacter());
						if(curEntries!=null)
						{
							curEntries.removeAll(Collections.singleton(null));
							for(ItemPopupEntry entry:curEntries)
							{
								html += "<a onclick='"+WebUtils.jsSafe(entry.clickJavascript)+"' minitip='"+WebUtils.jsSafe(entry.description)+"'>"+entry.name+"</a>";
							}
						}
					}
				}
			}
			
			html += "</div>";
			
			
			Map<String,String> formattedItem = new HashMap<>();
			
			formattedItem.put("html", html);
			if (item.getAttribute("proceduralKey")!=null)
				formattedItem.put("id", (String)item.getAttribute("proceduralKey"));
			else
				formattedItem.put("id", item.getKey().toString());
			
			itemsFormatted.add(formattedItem);
		}
		
		request.setAttribute("items", itemsFormatted);
		
		
	    return "/WEB-INF/odppages/ajax_gridmapcellcontents.jsp";
	}

}
