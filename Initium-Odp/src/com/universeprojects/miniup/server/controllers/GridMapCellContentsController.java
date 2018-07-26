package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.InitiumPageController;
import com.universeprojects.miniup.server.NotLoggedInException;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.services.GridMapService;

public class GridMapCellContentsController extends InitiumPageController
{

	public GridMapCellContentsController(String pageName)
	{
		super(pageName);
	}

	@Override
	protected String processInitiumRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		ODPDBAccess db = ODPDBAccess.getInstance(request);
		try{InitiumPageController.requireLoggedIn(db);}catch(NotLoggedInException e){return InitiumPageController.loginMessagePage;}
		
		int tileX = Integer.parseInt(request.getParameter("tileX"));
		int tileY = Integer.parseInt(request.getParameter("tileY"));
		
		CachedEntity location = db.getCharacterLocation(db.getCurrentCharacter());
		
		GridMapService gmService = new GridMapService(db, location);
		
		List<CachedEntity> items = gmService.generateTileItems(tileX, tileY);
		
		List<String> itemsFormatted = new ArrayList<>();
		for(CachedEntity item:items)
			itemsFormatted.add(GameUtils.renderItem(item));
		
		request.setAttribute("items", itemsFormatted);
		
		
	    return "/WEB-INF/odppages/ajax_gridmapcellcontents.jsp";
	}

}
