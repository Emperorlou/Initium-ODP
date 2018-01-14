package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.cacheddatastore.EntityPool;
import com.universeprojects.cacheddatastore.QueryHelper;
import com.universeprojects.miniup.CommonChecks;
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
	    QueryHelper query = new QueryHelper(ds);

	    List<CachedEntity> buyOrdersPT = query.getFilteredList("BuyItem", 
	    		"specialId", "premiumToken");	    
	    
	    List<CachedEntity> buyOrdersCT = query.getFilteredList("BuyItem", 
	    		"specialId", "chippedToken");	    
	    
	    List<CachedEntity> saleItems = query.getFilteredList("SaleItem", 
	    		"specialId", "Initium Premium Membership",
	    		"status", "Selling");

	    // First go through each sale item and extract the item keys for each
	    List<Key> itemKeys = new ArrayList<Key>();
	    List<Key> sellingCharacterKeys = new ArrayList<Key>();
	    for(CachedEntity saleItem:saleItems)
	    {
	    	itemKeys.add((Key)saleItem.getProperty("itemKey"));
	    	sellingCharacterKeys.add((Key)saleItem.getProperty("characterKey"));
	    }
	    
	    for(CachedEntity buyOrder:buyOrdersPT)
	    	sellingCharacterKeys.add((Key)buyOrder.getProperty("characterKey"));
	    
	    for(CachedEntity buyOrder:buyOrdersCT)
	    	sellingCharacterKeys.add((Key)buyOrder.getProperty("characterKey"));
	    
	    
	    
	    // Now batch get all the characters that sell these things
	    EntityPool pool = new EntityPool(ds);
	    pool.addToQueue(sellingCharacterKeys);
	    pool.addToQueue(itemKeys);
	    
	    pool.loadEntities();
	    
	    List<CachedEntity> items = pool.get(itemKeys);

	    // Now fetch the full item list (they will come back in the same order which we definitely want to take advantage of)
	    Map<CachedEntity, CachedEntity> itemToSaleItemMap = new HashMap<CachedEntity, CachedEntity>();
	    
	    
	    // Now go through the sale items and remove any saleItems (and item) that are invalid or hidden
	    for(int i = saleItems.size()-1; i>=0; i--)
	    {
	    	CachedEntity saleItem = saleItems.get(i);
			CachedEntity item = pool.get((Key)saleItem.getProperty("itemKey"));
	    	CachedEntity sellingCharacter = pool.get((Key)saleItem.getProperty("characterKey"));	
	        
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
		Double storeSale = (Double)sellingCharacter.getProperty("storeSale");
		if(storeSale==null) storeSale = Double.valueOf(100);
		Long coins = (Long)saleItem.getProperty("dogecoins");
		coins = Long.valueOf(Math.round(coins.doubleValue()*((storeSale.doubleValue())/100d)));    
		    
	        item.setProperty("store-dogecoins", (Object)coins);
	        item.setProperty("store-status", saleItem.getProperty("status"));
	        item.setProperty("store-saleItemKey", saleItem.getKey());
	        
	        // Also add the item to the map we're going to use
	        itemToSaleItemMap.put(item, saleItem);
	    }
	    items = db.sortSaleItemList(items);
	    
	    List<String> formattedPremiumTokens = new ArrayList<String>();
	    List<String> formattedChippedTokens = new ArrayList<String>();
	    
	    for(CachedEntity item:items)
        {
        	CachedEntity saleItem = itemToSaleItemMap.get(item);
        	if (CommonChecks.checkItemIsPremiumToken(item))
        		formattedPremiumTokens.add(HtmlComponents.generateStoreItemHtml(db,character, pool.get((Key)saleItem.getProperty("characterKey")),item,saleItem,request));
        	else if (CommonChecks.checkItemIsChippedToken(item))
        		formattedChippedTokens.add(HtmlComponents.generateStoreItemHtml(db,character, pool.get((Key)saleItem.getProperty("characterKey")),item,saleItem,request));
        		
        }
	    
	    request.setAttribute("premiumTokens", formattedPremiumTokens);
	    request.setAttribute("chippedTokens", formattedChippedTokens);

	    
	    
	    
	    
	    // Do buy orders now...

	    sortBuyOrders(buyOrdersPT);
	    sortBuyOrders(buyOrdersCT);
	    
	    List<String> formattedPremiumTokenBuyOrders = new ArrayList<>();
	    List<String> formattedChippedTokenBuyOrders = new ArrayList<>();
	    
	    for(int i = 0; i<buyOrdersPT.size(); i++)
	    {
	    	CachedEntity sellingCharacter = pool.get((Key)buyOrdersPT.get(i).getProperty("characterKey"));	
	        
	    	// If the character died and his buy order is still around, delete the buy order
	    	if (sellingCharacter==null)
	    	{
	    		ds.delete(buyOrdersPT.get(i));
	    		continue;
	    	}
	    	
			// If the character isn't currently vending, then don't sell
			if (ODPDBAccess.CHARACTER_MODE_MERCHANT.equals(sellingCharacter.getProperty("mode"))==false)
				continue;
	    	
	    	formattedPremiumTokenBuyOrders.add(HtmlComponents.generateBuyOrderHtml(db, buyOrdersPT.get(i), request));
	    }
	    
	    for(int i = 0; i<buyOrdersCT.size(); i++)
	    {
	    	CachedEntity sellingCharacter = pool.get((Key)buyOrdersCT.get(i).getProperty("characterKey"));	
	        
	    	// If the character died and his buy order is still around, delete the buy order
	    	if (sellingCharacter==null)
	    	{
	    		ds.delete(buyOrdersPT.get(i));
	    		continue;
	    	}
	    	
			// If the character isn't currently vending, then don't sell
			if (ODPDBAccess.CHARACTER_MODE_MERCHANT.equals(sellingCharacter.getProperty("mode"))==false)
				continue;
			
	    	formattedChippedTokenBuyOrders.add(HtmlComponents.generateBuyOrderHtml(db, buyOrdersCT.get(i), request));
	    }
	    
	    
	    request.setAttribute("premiumTokenBuyOrders", formattedPremiumTokenBuyOrders);
	    request.setAttribute("chippedTokenBuyOrders", formattedChippedTokenBuyOrders);
	    
	    
	    return "/WEB-INF/odppages/ajax_exchange.jsp";
	}
	
	
	private void sortBuyOrders(List<CachedEntity> list)
	{
		Collections.sort(list, new Comparator<CachedEntity>(){

			@Override
			public int compare(CachedEntity o1, CachedEntity o2)
			{
				return ((Long)o1.getProperty("value")).compareTo(((Long)o2.getProperty("value")));
			}
			
		});
		
		Collections.reverse(list);
	}
}
