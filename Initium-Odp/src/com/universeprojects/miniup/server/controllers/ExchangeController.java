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
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.HtmlComponents;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

@Controller
public class ExchangeController extends PageController {
	
	public ExchangeController() {
		super("ajax_exchange");
	}
	
	@Override
	protected final String processRequest(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
		
		response.setHeader("Access-Control-Allow-Origin", "*");     // This is absolutely necessary for phonegap to work

		ODPDBAccess db = ODPDBAccess.getInstance(request);
	    
	    CachedEntity character = db.getCurrentCharacter(); 
	    
	    CachedDatastoreService ds = db.getDB();
	    List<CachedEntity> saleItems = db.getFilteredList("SaleItem", 
	    		"specialId", "Initium Premium Membership",
	    		"status", "Selling");

	    // First go through each sale item and extract the item keys for each
	    List<Key> itemKeys = new ArrayList<Key>();
	    Map<Key,CachedEntity> sellingCharacters = new HashMap<Key,CachedEntity>();
	    List<Key> sellingCharacterKeys = new ArrayList<Key>();
	    for(CachedEntity saleItem:saleItems)
	    {
	    	itemKeys.add((Key)saleItem.getProperty("itemKey"));
	    	sellingCharacterKeys.add((Key)saleItem.getProperty("characterKey"));
	    }
	    
	    // Now batch get all the characters that sell these things
	    List<CachedEntity> sellingCharactersList = ds.fetchEntitiesFromKeys(sellingCharacterKeys);
	    for(CachedEntity c:sellingCharactersList)
	    	sellingCharacters.put(c.getKey(), c);

	    // Now fetch the full item list (they will come back in the same order which we definitely want to take advantage of)
	    List<CachedEntity> items = ds.fetchEntitiesFromKeys(itemKeys);
	    Map<CachedEntity, CachedEntity> itemToSaleItemMap = new HashMap<CachedEntity, CachedEntity>();
	    
	    
	    // Now go through the sale items and remove any saleItems (and item) that are invalid or hidden
	    for(int i = saleItems.size()-1; i>=0; i--)
	    {
	    	CachedEntity saleItem = saleItems.get(i);
			CachedEntity item = items.get(i);
	    	CachedEntity sellingCharacter = sellingCharacters.get((Key)saleItem.getProperty("characterKey"));	
	        
	        // If the item being sold was not found in the database, then we'll delete the sale item while we're at it
	        // OR
	        // If the item isn't in the seller's inventory AND the item is not sold, then lets delete the sellItem
	        if (item==null || GameUtils.equals(item.getProperty("containerKey"), sellingCharacter.getKey())==false)
	        {
	            ds.delete(saleItem);
	            saleItems.remove(i);
	            items.remove(i);
	            continue;
	        }
	        
			// If the character isn't currently vending, then don't sell
			if (ODPDBAccess.CHARACTER_MODE_MERCHANT.equals(sellingCharacter.getProperty("mode"))==false)
			{
				saleItems.remove(i);
				items.remove(i);
				continue;
			}
	        
	        
	        // These are only used for the sorting method db.sortSaleItemList()
	        item.setProperty("store-dogecoins", saleItem.getProperty("dogecoins"));
	        item.setProperty("store-status", saleItem.getProperty("status"));
	        item.setProperty("store-saleItemKey", saleItem.getKey());
	        
	        // Also add the item to the map we're going to use
	        itemToSaleItemMap.put(item, saleItem);
	    }
	    items = db.sortSaleItemList(items);
	    
	    List<String> formattedItems = new ArrayList<String>();
	    
	    for(CachedEntity item:items)
        {
        	CachedEntity saleItem = itemToSaleItemMap.get(item);
        	formattedItems.add(HtmlComponents.generateStoreItemHtml(db,character, sellingCharacters.get(saleItem.getProperty("characterKey")),item,saleItem,request));
        }
	    
	    request.setAttribute("items", formattedItems);
	    
	    return "/WEB-INF/odppages/ajax_exchange.jsp";
	}
}
