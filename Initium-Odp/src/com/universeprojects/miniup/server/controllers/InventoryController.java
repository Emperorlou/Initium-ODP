package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.InitiumPageController;
import com.universeprojects.miniup.server.NotLoggedInException;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

@Controller
public class InventoryController extends PageController {

	public InventoryController() {
		super("inventorylist");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		ODPDBAccess db = ODPDBAccess.getInstance(request);
		try{InitiumPageController.requireLoggedIn(db);}catch(NotLoggedInException e){return InitiumPageController.loginMessagePage;}

		CachedDatastoreService ds = db.getDB();
		CachedEntity character = db.getCurrentCharacter();

		// Get all the items for the character
		List<CachedEntity> items = db.getItemContentsFor(character.getKey(), false);
		items = db.sortSaleItemList(items);

		List<CachedEntity> saleItems = db.getFilteredList("SaleItem", "characterKey", character.getKey());
		// Store the item IDs from the SaleItems, so we can quickly check if
		// something is being sold.
		Set<Long> sellingItemIds = new HashSet<Long>();
		for (CachedEntity sItem : saleItems) {
			Key itemKey = (Key) sItem.getProperty("itemKey");
			if (itemKey != null)
				sellingItemIds.add(itemKey.getId());
		}

		// Store the equipped items, so we can quickly skip these when
		// generating items.
		Set<Long> equipItemIds = new HashSet<Long>();
		for (String slot : ODPDBAccess.EQUIPMENT_SLOTS) {
			Key equipKey = (Key) character.getProperty("equipment" + slot);
			if (equipKey != null)
				equipItemIds.add(equipKey.getId());
		}

		List<CachedEntity> carryingChars = db.getFilteredList("Character",
				"locationKey", character.getKey());
		if (carryingChars.isEmpty() == false) {
			List<String> carriedChars = new ArrayList<String>();
			for (CachedEntity c : carryingChars) {
				String currentChar = "<div class='main-item-container'><br/>";
				currentChar += "	" + GameUtils.renderCharacter(null, c, true, false);
				currentChar += "	<div class='main-item-controls'>";
				currentChar += "		<a onclick='characterDropCharacter(event, " + c.getId() + ")'>Put on ground</a>";
				currentChar += "	</div>";
				currentChar += "</div>";
				carriedChars.add(currentChar);
			}
			request.setAttribute("carriedCharacters", carriedChars);
		}
		request.setAttribute("isCarryingCharacters",
				carryingChars.isEmpty() == false);

		List<String> itemOutputs = new ArrayList<String>();
		String currentCategory = "";
		for (CachedEntity item : items) {
			
			if (equipItemIds.contains(item.getId()))
				continue;
			
			StringBuilder sb = new StringBuilder();
			String itemType = (String) item.getProperty("itemType");
			if (itemType == null)
				itemType = "";

			if (currentCategory.equals(itemType) == false) {
				sb.append("<h4> " + itemType + "</h4>");
				currentCategory = itemType;
			}

			sb.append(GameUtils.renderInventoryItem(db, item, character, sellingItemIds.contains(item.getId())));

			itemOutputs.add(sb.toString());
		}
		request.setAttribute("itemList", itemOutputs);

		return "/WEB-INF/odppages/inventorylist.jsp";
	}

}
