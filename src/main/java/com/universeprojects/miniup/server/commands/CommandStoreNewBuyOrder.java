package com.universeprojects.miniup.server.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Query.FilterOperator;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.HtmlComponents;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
/** 
 * 
 * Sell an Item!
 * 
 */

public class CommandStoreNewBuyOrder extends Command {
	
	public CommandStoreNewBuyOrder(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}
	
	@Override
	public void run(Map<String,String> parameters) throws UserErrorMessage 
	{
		
		if(CommonChecks.checkCharacterIsZombie(db.getCurrentCharacter()))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");
		
		String itemName = parameters.get("itemName");
		Long value = Long.parseLong(parameters.get("value"));
		Long quantity = null;
		if (parameters.get("quantity")!=null && GameUtils.equals("", parameters.get("quantity"))==false) 
			quantity = Long.parseLong(parameters.get("quantity"));
		
		if (quantity!=null && quantity<0)
			throw new UserErrorMessage("Requested quantity cannot be less than 0.");
		
		if (value<0)
			throw new UserErrorMessage("You cannot buy items for less than 0 gold.");
		
		ODPDBAccess db = getDB();

		List<CachedEntity> sample = query.getFilteredList("Item", 1, "name", FilterOperator.EQUAL, itemName);
		if (sample==null || sample.isEmpty())
			throw new UserErrorMessage("There is no item in the game world that goes by the name '"+itemName+"'.");
		
//		if (sample.get(0).getProperty("quantity")==null)
//			throw new UserErrorMessage("You currently can only create a buy order for stackable items. Sorry!");
		
		List<List<String>> fieldFilters = new ArrayList<>();
		fieldFilters.add(Arrays.asList(new String[]{"name=="+itemName}));
		
		CachedEntity buyOrder = new CachedEntity("BuyItem");
		buyOrder.setProperty("name", itemName);
		buyOrder.setProperty("characterKey", db.getCurrentCharacterKey());
		db.setValue_FieldTypeFieldFilter2DCollection(buyOrder, "fieldFilters", fieldFilters);
		buyOrder.setProperty("name", itemName);
		buyOrder.setProperty("quantity", quantity);
		buyOrder.setProperty("value", value);
		
		// This is a special case
		if ("Initium Premium Membership".equals(itemName))
		{
			setPopupMessage("This buy order has been listed on the global exchange for premium tokens.");
			buyOrder.setProperty("specialId", "premiumToken");
		}
		
		// This is a special case
		if ("Chipped Token".equals(itemName))
		{
			setPopupMessage("This buy order been listed on the global exchange for premium tokens.");
			buyOrder.setProperty("specialId", "chippedToken");
		}
		
		ds.put(buyOrder);
		
		addCallbackData("createBuyOrder", HtmlComponents.generateManageStoreBuyOrderHtml(db, buyOrder, request));
	}
}
