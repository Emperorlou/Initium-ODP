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
import com.universeprojects.miniup.server.aspects.AspectSlottable;
import com.universeprojects.miniup.server.aspects.AspectSlotted;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

/**
 * This controller filters the user's inventory items to show items that have the slottable aspect.
 * @author Evan
 *
 */
@Controller
public class SlottableItemsListController extends PageController {
	
	public SlottableItemsListController() {
		super("slottableitemslist");
	}

	@Override
	protected final String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ODPDBAccess db = ODPDBAccess.getInstance(request);
		try{InitiumPageController.requireLoggedIn(db);}catch(NotLoggedInException e){return InitiumPageController.loginMessagePage;}
		
		try {
			List<Map<String,String>> itemsFormatted = new ArrayList<>();
			
			Long baseId = Long.parseLong(request.getParameter("baseId"));
			
			CachedEntity baseEntity = db.getEntity("Item", baseId);
			if (baseEntity == null) throw new UserErrorMessage("This Item no longer exists.");
			String itemName = (String)baseEntity.getProperty("name");
			InitiumObject base = new InitiumObject(db, baseEntity);
			AspectSlotted baseAspect = base.getAspect(AspectSlotted.class);
			if (baseAspect==null) throw new UserErrorMessage("This item has no slots!");
						
			CachedEntity character = db.getCurrentCharacter();
	
			QueryHelper query = new QueryHelper(db.getDB());

			List<CachedEntity> items = query.getFilteredList("Item", "containerKey", db.getCurrentCharacterKey());
			
			// Now filter out all the items that are not compatible
			List<CachedEntity> filteredItems = db.filterEntitiesByAspect(items, "Slottable");

			String slotValidator = (String) baseAspect.getProperty("slotValidator");
			
			for(CachedEntity item:filteredItems) {
				if (GameUtils.equals(item.getId(), baseEntity.getId())) continue;
				
				//Compare the validator on the slottable aspect with the validator on the slotted aspect.
				InitiumObject itemIO = new InitiumObject(db, item);
				AspectSlottable as = itemIO.getAspect(AspectSlottable.class);
				if (!GameUtils.equals(slotValidator, (String) as.getProperty("slotValidator"))) continue;
				
				Map<String,String> data = new HashMap<>();
				data.put("html", GameUtils.renderItem(db, character, item));
				data.put("id", item.getId().toString());
				
				itemsFormatted.add(data);
			}
			request.setAttribute("baseIcon", GameUtils.getResourceUrl(baseEntity.getProperty("icon")));
			request.setAttribute("baseId", baseId);
			request.setAttribute("items", itemsFormatted);
			request.setAttribute("maxSlots", GameUtils.formatNumber(baseAspect.getMaxCount()));
			request.setAttribute("openSlots", GameUtils.formatNumber(baseAspect.getOpenCount()));
		}
		catch(UserErrorMessage e) {
			request.setAttribute("error", e.getMessage());
		}
		return "/WEB-INF/odppages/ajax_slottableitemslist.jsp";
	}
}