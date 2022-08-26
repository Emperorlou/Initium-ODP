package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.gef.WebUtils;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.InitiumPageController;
import com.universeprojects.miniup.server.NotLoggedInException;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.CustomizationStoreService;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

@Controller
public class CustomStoreController extends PageController {
	
	public CustomStoreController() {
		super("customstore");
	}

	@Override
	protected final String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		ODPDBAccess db = ODPDBAccess.getInstance(request);
		try{InitiumPageController.requireLoggedIn(db);}catch(NotLoggedInException e){return InitiumPageController.loginMessagePage;}
		CachedDatastoreService ds = db.getDB();
	
		Long itemId = WebUtils.getLongParam(request, "itemId");
		final CachedEntity item = itemId!=null ? db.getEntity("Item", itemId) : null;
	
		CustomizationStoreService css = new CustomizationStoreService(db);
		
		List<CachedEntity> buyableList = css.getBuyableList(item);
		List<CachedEntity> allBuyableList = css.getBuyableList(null);
		
		if (item != null) {
			request.setAttribute("itemRendered", GameUtils.renderItem(item));
			request.setAttribute("itemId", itemId);
		}
		request.setAttribute("buyables", transformBuyableListForUI(buyableList));
		request.setAttribute("allBuyables", transformBuyableListForUI(allBuyableList));
		request.setAttribute("isBuyablesAvailable", buyableList.size() > 0);
		
		
		try {
		
			if (item==null)
				request.setAttribute("noItem", true);
			else
				request.setAttribute("noItem", false);
			if (itemId==null)
				request.setAttribute("itemId", "null");
			else
				request.setAttribute("itemId", itemId);
				
				
			
			// If the item isn't in our inventory then complain about that too
			if (item!=null && GameUtils.equals(item.getProperty("containerKey"), db.getCurrentCharacterKey())==false)
			{
				throw new UserErrorMessage("The item is not in your character's inventory and so cannot be customized.");
			}
			
			
			Long totalDonations = (Long)db.getCurrentUser().getProperty("totalDonations");
			if (totalDonations==null) totalDonations = 0L;
			request.setAttribute("totalDonations", GameUtils.formatNumber(totalDonations.doubleValue(), true));
			
			Boolean viewAll = WebUtils.getBoolParam(request, "viewAll", true);
			request.setAttribute("viewAll", viewAll);
			
			
			return "/WEB-INF/odppages/ajax_customstore.jsp";
		} catch (UserErrorMessage e) {
			request.setAttribute("error", e.getMessage());
			return "/WEB-INF/odppages/ajax_customstore.jsp";
		}
	} 


	private Map<String, List<Map<String, Object>>> transformBuyableListForUI(List<CachedEntity> list) {
		
		Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();

		// First sort the incoming list by the applied types
		list.sort(new Comparator<CachedEntity>() {
			@Override
			public int compare(CachedEntity e1, CachedEntity e2) {
				if (e1.getProperty("cost") == null || e2.getProperty("cost") == null) return 0;
				
				return ((Long)e1.getProperty("cost")).compareTo((Long)e2.getProperty("cost"));
			}
		});
		
		
		for(CachedEntity customDef:list) {
			String icon = (String) customDef.getProperty("icon");
			String largeImage = (String) customDef.getProperty("largeImage");
			String effectOverlay = (String) customDef.getProperty("effectOverlay");
			Double effectOverlayBrightness = (Double) customDef.getProperty("effectOverlayBrightness");
			Long cost = (Long) customDef.getProperty("cost");
			Date saleExpiryDate = (Date) customDef.getProperty("saleExpiryDate");
			String rarity = (String) customDef.getProperty("rarity");
			String applyText = generateApplyTextFor(customDef);
			
			// Sanitize
			if (icon == null) continue;
			if (rarity == null) rarity = "Common";	 
			rarity = rarity.toLowerCase();
			
			// Get the correct category 
			List<Map<String, Object>> category = result.get(applyText);
			if (category == null) {
				category = new ArrayList<>();
				result.put(applyText, category);
			}
			
			
			Map<String, Object> data = new HashMap<>();
			data.put("id", customDef.getId());
			data.put("icon", GameUtils.getResourceUrl(icon));
			data.put("largeImage", GameUtils.getResourceUrl(largeImage));
			data.put("effectOverlay", GameUtils.getResourceUrl(effectOverlay)); 
			data.put("effectOverlayBrightness", effectOverlayBrightness);
			data.put("cost", cost);
			data.put("costFormatted", GameUtils.formatNumber(cost, false));
			if (saleExpiryDate != null) 
				data.put("expiryDateFormatted", GameUtils.getTimePassedShortString(saleExpiryDate));
			data.put("applyText", applyText);
			data.put("rarity", rarity);
			
			
			category.add(data);
		}
		
		return result;
	}
	
	private String generateApplyTextFor(CachedEntity customDef) {
		String appliedEquipSlot = (String) customDef.getProperty("appliedEquipSlot");
		String appliedItemType = (String) customDef.getProperty("appliedItemType");
		String appliedDamageType = (String) customDef.getProperty("appliedDamageType");
		
		String applyText = "";
		if (appliedEquipSlot != null) {
			applyText += appliedEquipSlot.replaceAll("\\s*,\\s*", " or ").replaceAll("([a-z])([A-Z])", "$1 $2");
		}
		if (appliedItemType != null) {
			applyText += " " + appliedItemType.replaceAll("\\s*,\\s*", " or ").replaceAll("([a-z])([A-Z])", "$1 $2");
		}

		if (appliedDamageType != null) {
			applyText += " (" + appliedDamageType.replaceAll("\\s*,\\s*", " or ").replaceAll("([a-z])([A-Z])", "$1 $2") + " only)";
		}

		return applyText;
	}

}