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
    GameFunctions db = auth.getDB(request);
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
        WebUtils.forceRedirectClientTo("main.jsp", request, response, "This store no longer exists.");
        return;
    }
    
    if ("MERCHANT".equals(storeCharacter.getProperty("mode"))==false)
    {
        WebUtils.forceRedirectClientTo("main.jsp", request, response, "The store you're trying to browse is now closed. The player has shut it down.");
        return;
    }
    
    if (((Key)storeCharacter.getProperty("locationKey")).getId()!=common.getLocation().getKey().getId())
    {
        WebUtils.forceRedirectClientTo("main.jsp", request, response, "The store you're trying to browse is not in your location.");
        return;
    }
    
    
    List<CachedEntity> saleItems = db.getFilteredList("SaleItem", "characterKey", storeCharacter.getKey());
    
    List<CachedEntity> items = new ArrayList<CachedEntity>();
    CachedDatastoreService ds = db.getDB();
    for(CachedEntity saleItem:saleItems)
    {
        if ("Hidden".equals(saleItem.getProperty("status")))
            continue;
        
        CachedEntity item = db.getEntity((Key)saleItem.getProperty("itemKey"));
        if (item==null)
        {
            ds.delete(saleItem);
            continue;
        }
        item.setProperty("store-dogecoins", saleItem.getProperty("dogecoins"));
        item.setProperty("store-status", saleItem.getProperty("status"));
        item.setProperty("store-saleItemKey", saleItem.getKey());
        
        items.add(item);
    }
    items = db.sortSaleItemList(items);
    
    
    Double storeSale = (Double)storeCharacter.getProperty("storeSale");
    if (storeSale==null) storeSale = 100d;
    request.setAttribute("storeSale", storeSale);
    
    // TODO: Check if all saleItems are sold, if so, take the player out of store mode
%>
        <%=GameUtils.renderSimpleBanner("images/banner---inventory.jpg")%>
        
        
        <div>
        <h4><%=storeCharacter.getProperty("name")%>'s Storefront</h4> 
        <p><%=storeCharacter.getProperty("storeName")%></p>
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
                out.println(HtmlComponents.generateStoreItemHtml(db,storeCharacter,item,saleItem,request));
            }
        %>
        </div>
