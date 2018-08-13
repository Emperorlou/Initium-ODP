package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.InitiumAspect;
import com.universeprojects.miniup.server.InitiumObject;
import com.universeprojects.miniup.server.InitiumPageController;
import com.universeprojects.miniup.server.ItemAspect;
import com.universeprojects.miniup.server.NotLoggedInException;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.WebUtils;
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
			String html = GameUtils.renderItem(db, request, db.getCurrentCharacter(), item, false, false, (String)item.getAttribute("proceduralKey"));
			
			html += "<div class='main-item-controls'>";
			InitiumObject iObject = new InitiumObject(db, item);
			if (iObject.hasAspects())
			{
				// Go through the aspects on this item and include any special links that it may have
				for(InitiumAspect initiumAspect:iObject.getAspects())
				{
					if (initiumAspect instanceof ItemAspect)
					{
						ItemAspect itemAspect = (ItemAspect)initiumAspect;
						
						List<ItemPopupEntry> curEntries = itemAspect.getItemPopupEntries();
						if(curEntries!=null)
						{
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
