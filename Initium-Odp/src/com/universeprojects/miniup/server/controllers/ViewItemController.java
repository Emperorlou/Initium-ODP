package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.InitiumAspect;
import com.universeprojects.miniup.server.InitiumObject;
import com.universeprojects.miniup.server.ItemAspect;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.WebUtils;
import com.universeprojects.miniup.server.ItemAspect.ItemPopupEntry;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

@Controller
public class ViewItemController extends PageController {

	public ViewItemController() {
		super("viewitemmini");
	}

	@Override
	protected final String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		response.setHeader("Access-Control-Allow-Origin", "*");     // This is absolutely necessary for phonegap to work

		ODPDBAccess db = ODPDBAccess.getInstance(request);
	    CachedDatastoreService ds = db.getDB();
	    CachedEntity character = db.getCurrentCharacter(); 
	    
	    Long itemId = WebUtils.getLongParam(request, "itemId");
		if (itemId==null) return null;
		
		Key itemKey = KeyFactory.createKey("Item", itemId);
		CachedEntity item = db.getEntity(itemKey); 
		if (item==null)
		{
			response.sendError(404);
			return null;
		}
		
		if(character == null)
		{
			return null;
		}
		
		// This is a special case for premium tokens...
		if (CommonChecks.checkItemIsPremiumToken(item))
			request.setAttribute("showPremiumTokenUI", true);
		
		if (CommonChecks.checkItemIsChippedToken(item))
			request.setAttribute("showChippedTokenUI", true);
		
		request.setAttribute("isItemOwner", GameUtils.equals(item.getProperty("containerKey"), character.getKey()));
		
		String itemHtml = GameUtils.renderItemMini(db, character, item, false);
		request.setAttribute("itemHtml", itemHtml);
		
		List<String> comparisons = new ArrayList<String>();
		String equipSlot = (String)item.getProperty("equipSlot");
		if (equipSlot!=null)
		{
			if (equipSlot.equals("2Hands")) equipSlot = "LeftHand and RightHand";
			String[] equipSlots = equipSlot.split("(,| and )");
			for(String slot:equipSlots)
			{
				CachedEntity equipment = db.getEntity((Key)character.getProperty("equipment"+slot));
				if (equipment!=null)
					comparisons.add(GameUtils.renderItemMini(db, character, equipment, true));
			}
		}
		
		if(comparisons.isEmpty() == false)
			request.setAttribute("comparisons", comparisons);
		
		return "/WEB-INF/odppages/viewitemmini.jsp";
	}

}
