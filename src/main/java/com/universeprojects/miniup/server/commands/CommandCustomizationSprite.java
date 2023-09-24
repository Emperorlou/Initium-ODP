package com.universeprojects.miniup.server.commands;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.AbortTransactionException;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.Transaction;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.ODPDBAccess.CustomOrderStatus;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.WebUtils;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class CommandCustomizationSprite extends Command
{


	final String regexValidator = "[A-Za-z0-9'\", \\-\\(\\)/\\.?!&$%#\r\n]+";
	public CommandCustomizationSprite(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException
	{
		CachedEntity character = db.getCurrentCharacter();
		
		if(CommonChecks.checkCharacterIsZombie(character))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");
		
		Long itemId = WebUtils.getLongParam(request, "itemId");
		CachedEntity item = db.getEntity("Item", itemId);
		
		if (item == null)
			throw new UserErrorMessage("You cannot buy a customization without first selecting an item to apply it to. "
					+ "Click on an item to customize, and then click on the Customize link at the top to customize it.");
		
		if (item.getProperty("quantity")!=null)
			throw new UserErrorMessage("You cannot customize a stackable item.");
		
		// This is the Custom Item - Name and Flavor Text order type
		Long customStoreItemDefId = WebUtils.getLongParam(request, "customStoreItemDefId");
		Key typeKey = KeyFactory.createKey("CustomizationStoreItemDef", customStoreItemDefId);

		CachedEntity customStoreItemDef = db.getEntity(typeKey);
		if (customStoreItemDef == null) 
			throw new UserErrorMessage("The customization you've ordered does not exist.");
		
		
		if (GameUtils.equals(customStoreItemDef.getProperty("status"), "Live") == false)
			throw new UserErrorMessage("This item is not currently for sale.");
		
		// This is mostly just done for tracking purposes
		doOrderItemSpriteUpdate(db.getCurrentUser(), db.getCurrentCharacter(), item, typeKey);
		
		
		item.setProperty("icon", customStoreItemDef.getProperty("icon"));
		item.setProperty("largeImage", customStoreItemDef.getProperty("largeImage"));
		item.setProperty("effectOverlay", customStoreItemDef.getProperty("effectOverlay"));
		item.setProperty("effectOverlayBrightness", customStoreItemDef.getProperty("effectOverlayBrightness"));
		
		db.getDB().put(item);

		setPopupMessage("Your order has been completed automatically.");
		
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
	}
	
	
	private void doOrderItemSpriteUpdate(final CachedEntity finalUser, final CachedEntity finalCharacter, final CachedEntity finalItem, Key customizationStoreItemDefKey) throws UserErrorMessage {
		try
		{
			CachedEntity order = new Transaction<CachedEntity>(getDS()){

				@Override
				public CachedEntity doTransaction(CachedDatastoreService ds) throws AbortTransactionException
				{
					CachedEntity customizationStoreItemDef = db.getEntity(customizationStoreItemDefKey);
					if (customizationStoreItemDef==null)
						throw new AbortTransactionException("Invalid order type specified.");

					// Gather all the variables for the order type
					Long cost = (Long)customizationStoreItemDef.getProperty("cost");
					String icon = (String)customizationStoreItemDef.getProperty("icon");
					if (cost==null || icon==null)
						throw new AbortTransactionException("Order type is malformed.");
					
				
					CachedEntity item = refetch(finalItem);
					CachedEntity user = refetch(finalUser);
					
					// Does the user have enough donation credit
					Long donationCredit = (Long)user.getProperty("totalDonations");
					if (donationCredit==null) donationCredit = 0L;
					if (donationCredit<cost)
						throw new AbortTransactionException("You do not have enough donation credit for this. You have "+GameUtils.formatNumber(donationCredit.doubleValue())+" but need "+GameUtils.formatNumber(cost.doubleValue())+".");
					
					CachedEntity order = null;
					if (item!=null && item.getKind().equals("Item"))
					{
						CachedEntity character = refetch(finalCharacter);
						
						// Is the item in our inventory
						if (GameUtils.equals(item.getProperty("containerKey"), character.getKey())==false)
							throw new AbortTransactionException("You cannot create a customization order for an item that is not currently in your inventory.");
						
					} else {
						throw new AbortTransactionException("Unsupported entity type: " + item.getKind());
					}
					
						
					order = new CachedEntity("CustomOrder");
					order.setProperty("active", true);
					order.setProperty("createdDate", new Date());
					order.setProperty("customizationStoreItemDefKey", customizationStoreItemDefKey);
					order.setProperty("description", "Sprite customization");
					order.setProperty("donationCredit", cost);
					if (item!=null)
						order.setProperty("entityKey", item.getKey());
					order.setProperty("lastUpdateDate", new Date());
					order.setProperty("status", CustomOrderStatus.Complete.toString());
					order.setProperty("active", false);
					order.setProperty("userKey", user.getKey());
						
					
					user.setProperty("totalDonations", donationCredit-cost);
					user.setProperty("usedCustomOrders", true);
					
					ds.put(order);
					ds.put(user);
					
					return order;
				}
				
				
			}.run();
			
			db.sendCustomizationStorePurchaseNotification(ds, order);
			
			
			
			db.sendGlobalMessage(finalCharacter.getProperty("name")+" has ordered an item customization! To order one for yourself, <a onclick='customizeItemOrderPage()'>check out this page</a>.");
		}
		catch (AbortTransactionException e)
		{
			throw new UserErrorMessage(e.getMessage());
		}
		
		
	}

}
