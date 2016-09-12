<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="com.universeprojects.miniup.server.HtmlComponents"%>
<%@page import="com.universeprojects.cacheddatastore.CachedEntity"%>
<%@page import="com.universeprojects.miniup.server.GameUtils"%>
<%@page import="com.universeprojects.cacheddatastore.CachedDatastoreService"%>
<%@page import="java.util.ArrayList"%>
<%@page import="com.universeprojects.miniup.server.WebUtils"%>
<%@page import="com.google.appengine.api.datastore.Key"%>
<%@page import="com.universeprojects.miniup.server.GameFunctions"%>
<%@page import="java.util.List"%>
<%@page import="com.universeprojects.miniup.server.SecurityException"%>
<%@page import="com.universeprojects.miniup.server.CommonEntities"%>
<%@page import="com.universeprojects.miniup.server.ErrorMessage"%>
<%@page import="com.universeprojects.miniup.server.JspSnippets"%>
<%@page import="com.google.appengine.api.datastore.Entity"%>
<%@page import="com.universeprojects.miniup.server.ODPDBAccess"%>
<%@page import="com.universeprojects.miniup.server.Authenticator"%>
<%
    response.setHeader("Access-Control-Allow-Origin", "*");     // This is absolutely necessary for phonegap to work
    Authenticator auth = Authenticator.getInstance(request);
    ODPDBAccess db = auth.getDB(request);
    try
    {
        auth.doSecurityChecks(request);
    }
    catch(SecurityException e)
    {
        JspSnippets.handleSecurityException(e, request, response);
        return;
    }
    
    CommonEntities common = CommonEntities.getInstance(request);
    
    Long characterId = WebUtils.getLongParam(request, "characterId");
    if (characterId==null)
        return;
    
    if (common.getCharacter().getKey().getId() == characterId)
    {
        WebUtils.forceRedirectClientTo("managestore.jsp", request, response);
        return;
    }
    
	CachedEntity storeCharacter = db.getEntity("Character", characterId);
	if (storeCharacter==null)
	{
		out.println("OMG the person who owned this store has been KILLED! You cannot browse their store anymore.");
		return;
	}
	
	if ("MERCHANT".equals(storeCharacter.getProperty("mode"))==false)
	{
		out.println("The store you're trying to browse is now closed. The player has shut it down.");
		return;
	}
	
	if (((Key)storeCharacter.getProperty("locationKey")).getId()!=common.getLocation().getKey().getId())
	{
		out.println("The store you're trying to browse is not in your location.");
		return;
	}

    
    
    CachedDatastoreService ds = db.getDB();
    List<CachedEntity> saleItems = db.getFilteredList("SaleItem", "characterKey", storeCharacter.getKey());
    List<Key> itemKeys = new ArrayList<Key>();
    
    // First go through each sale item and extract the item keys for each
    for(CachedEntity saleItem:saleItems)
    	itemKeys.add((Key)saleItem.getProperty("itemKey"));

    // Now fetch the full item list (they will come back in the same order which we definitely want to take advantage of)
    List<CachedEntity> items = ds.fetchEntitiesFromKeys(itemKeys);
    Map<CachedEntity, CachedEntity> itemToSaleItemMap = new HashMap<CachedEntity, CachedEntity>();
    
    
    // Now go through the sale items and remove any saleItems (and item) that are invalid or hidden
    for(int i = saleItems.size()-1; i>=0; i--)
    {
    	CachedEntity saleItem = saleItems.get(i);
		CachedEntity item = items.get(i);
    	
        if ("Hidden".equals(saleItem.getProperty("status")))
        {
        	saleItems.remove(i);
        	items.remove(i);
            continue;
        }
        
        // If the item being sold was not found in the database, then we'll delete the sale item while we're at it
        // OR
        // If the item isn't in the seller's inventory AND the item is not sold, then lets delete the sellItem
        if (item==null || 
        	(GameUtils.equals(item.getProperty("containerKey"),storeCharacter.getKey())==false && "Sold".equals(saleItem.getProperty("status"))==false))
        {
            ds.delete(saleItem);
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
    
    
    Double storeSale = (Double)storeCharacter.getProperty("storeSale");
    if (storeSale==null) storeSale = 100d;
    request.setAttribute("storeSale", storeSale);
    
    // TODO: Check if all saleItems are sold, if so, take the player out of store mode
%>
        <div>
        <h4><%=storeCharacter.getProperty("name")%>'s Storefront</h4> 
        <p><%=storeCharacter.getProperty("storeName")%></p>
        <div class="main-item-filter">
			<input class="main-item-filter-input" id="filter_saleItem" type="text" placeholder="Filter store items...">
		</div>
        <%
            String currentCategory = "";
            for(CachedEntity item:items)
            {
                String itemType = (String)item.getProperty("itemType");
                if (itemType==null) itemType = "Other";
                
                if (currentCategory.equals(itemType)==false)
                {
                    out.println("<h3>"+itemType+"</h3>");
                    currentCategory = itemType;
                }
                out.println(HtmlComponents.generateStoreItemHtml(db,common.getCharacter(), storeCharacter,item,itemToSaleItemMap.get(item),request));
            }
        %>
        </div>
