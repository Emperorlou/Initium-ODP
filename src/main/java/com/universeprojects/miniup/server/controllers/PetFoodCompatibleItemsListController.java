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
import com.universeprojects.miniup.server.InitiumObject;
import com.universeprojects.miniup.server.InitiumPageController;
import com.universeprojects.miniup.server.NotLoggedInException;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.aspects.AspectPet;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

@Controller
public class PetFoodCompatibleItemsListController extends PageController {
	
	public PetFoodCompatibleItemsListController() {
		super("petfoodcompatibleitemslist");
	}

	@Override
	protected final String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		ODPDBAccess db = ODPDBAccess.getInstance(request);
		try{InitiumPageController.requireLoggedIn(db);}catch(NotLoggedInException e){return InitiumPageController.loginMessagePage;}
		
		try
		{
			List<Map<String,String>> itemsFormatted = new ArrayList<>();
			
			Long petId = Long.parseLong(request.getParameter("petId"));
			
			// Prepare the buy order
			CachedEntity petEntity = db.getEntity("Item", petId);
			if (petEntity==null) throw new UserErrorMessage("This pet no longer exists.");
			String itemName = (String)petEntity.getProperty("name");
			InitiumObject pet = new InitiumObject(db, petEntity);
			AspectPet petAspect = pet.getAspect(AspectPet.class);
			if (petAspect==null) throw new UserErrorMessage("This is not a pet. You can only feed pet food to pets.");
			
			List<List<String>> fieldFilters = petAspect.getFieldFilters();
			
			CachedEntity character = db.getCurrentCharacter();
	
			QueryHelper query = new QueryHelper(db.getDB());

			List<CachedEntity> items = query.getFilteredList("Item", "containerKey", db.getCurrentCharacterKey());
			
			// Now filter out all the items that are not compatible
			petAspect.filterFoodItems(items);

			
			for(CachedEntity item:items)
			{
				if (GameUtils.equals(item.getId(), petEntity.getId()))
					continue;
				Map<String,String> data = new HashMap<>();
				data.put("html", GameUtils.renderItem(db, character, item));
				data.put("id", item.getId().toString());
				
				itemsFormatted.add(data);
			}
			request.setAttribute("petIcon", GameUtils.getResourceUrl(petEntity.getProperty("icon")));
			request.setAttribute("petId", petId);
			request.setAttribute("items", itemsFormatted);
			request.setAttribute("petFoodMax", GameUtils.formatNumber(petAspect.getDesiredFoodKg()));
		}
		catch(UserErrorMessage e)
		{
			request.setAttribute("error", e.getMessage());
		}
		
		return "/WEB-INF/odppages/ajax_petfoodcompatibleitemslist.jsp";
	}
	
}