package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Query.FilterOperator;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.QueryHelper;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.InitiumPageController;
import com.universeprojects.miniup.server.NotLoggedInException;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

@Controller
public class BuyOrderCompatibleItemsListController extends PageController {
	
	public BuyOrderCompatibleItemsListController() {
		super("buyordercompatibleitemslist");
	}

	@Override
	protected final String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		ODPDBAccess db = ODPDBAccess.getInstance(request);
		try{InitiumPageController.requireLoggedIn(db);}catch(NotLoggedInException e){return InitiumPageController.loginMessagePage;}
		
		try
		{
			List<Map<String,String>> itemsFormatted = new ArrayList<>();
			
			Long buyOrderId = Long.parseLong(request.getParameter("buyOrderId"));
			
			// Prepare the buy order
			CachedEntity buyOrder = db.getEntity("BuyItem", buyOrderId);
			if (buyOrder==null) throw new UserErrorMessage("This buy order no longer exists.");
			String itemName = (String)buyOrder.getProperty("name");
			
			
			CachedEntity character = db.getCurrentCharacter();
	
			QueryHelper query = new QueryHelper(db.getDB());

			List<CachedEntity> items = query.getFilteredList("Item", 20, null, 
					"containerKey", FilterOperator.EQUAL, db.getCurrentCharacterKey(),
					"name", FilterOperator.EQUAL, itemName);

			
			for(CachedEntity item:items)
			{
				if (CommonChecks.checkItemIsEquipped(item.getKey(), character))
					continue;
					
				Map<String,String> data = new HashMap<>();
				data.put("html", GameUtils.renderItem(db, character, item));
				data.put("id", item.getId().toString());
				
				itemsFormatted.add(data);
			}
			request.setAttribute("buyOrderId", buyOrderId);
			request.setAttribute("buyOrderValueEach", buyOrder.getProperty("value"));
			request.setAttribute("buyOrderMaxQuantity", buyOrder.getProperty("quantity"));
			if (buyOrder.getProperty("quantity")==null)
				request.setAttribute("buyOrderMaxQuantity", "null");
			request.setAttribute("buyOrderName", buyOrder.getProperty("name"));
			request.setAttribute("items", itemsFormatted);
		}
		catch(UserErrorMessage e)
		{
			request.setAttribute("error", e.getMessage());
		}
		
		return "/WEB-INF/odppages/ajax_buyordercompatibleitemslist.jsp";
	}
	
}