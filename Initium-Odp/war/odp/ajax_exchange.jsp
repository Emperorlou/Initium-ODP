<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.universeprojects.miniup.server.HtmlComponents"%>
<%@page import="com.universeprojects.cacheddatastore.CachedEntity"%>
<%@page import="com.universeprojects.miniup.server.GameUtils"%>
<%@page import="com.universeprojects.cacheddatastore.CachedDatastoreService"%>
<%@page import="java.util.ArrayList"%>
<%@page import="com.universeprojects.miniup.server.WebUtils"%>
<%@page import="com.google.appengine.api.datastore.Key"%>
<%@page import="java.util.List"%>
<%@page import="com.universeprojects.miniup.server.JspSnippets"%>
<%@page import="com.google.appengine.api.datastore.Entity"%>
<%@page import="com.universeprojects.miniup.server.ODPDBAccess"%>
<%
    response.setHeader("Access-Control-Allow-Origin", "*");     // This is absolutely necessary for phonegap to work

    ODPDBAccess db = new ODPDBAccess(request);
    
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
    
%>
        <div>
        <h4>Global Premium Membership Token Exchange</h4> 
        <p>This is a special system that is ONLY for buying and selling Initium Premium Membership tokens. Use gold to always buy at the lowest price quickly and easily.</p>
        <%
            for(CachedEntity item:items)
            {
            	CachedEntity saleItem = itemToSaleItemMap.get(item);
                out.println(HtmlComponents.generateStoreItemHtml(db,character, sellingCharacters.get(saleItem.getProperty("characterKey")),item,saleItem,request));
            }
        %>
        </div>
