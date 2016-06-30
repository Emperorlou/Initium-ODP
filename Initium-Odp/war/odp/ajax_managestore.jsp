<%@page import="com.universeprojects.miniup.server.HtmlComponents"%>
<%@page import="com.universeprojects.miniup.server.HiddenUtils"%>
<%@page import="com.universeprojects.cacheddatastore.CachedEntity"%>
<%@page import="com.universeprojects.miniup.server.GameUtils"%>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
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
	response.setHeader("Access-Control-Allow-Origin", "*");		// This is absolutely necessary for phonegap to work
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
	
	List<CachedEntity> items = db.getEntityListFrom(common.getCharacter().getKey(), "inventory");
	items = db.sortSaleItemList(items);
	
	List<CachedEntity> saleItems = db.getFilteredList("SaleItem", "characterKey", common.getCharacter().getKey());
	
	
	boolean storeOpen = false;
	if ("MERCHANT".equals(common.getCharacter().getProperty("mode")))
	{
		storeOpen = true;
	}
	request.setAttribute("storeOpen", storeOpen);
		
	
	Double storeSale = (Double)common.getCharacter().getProperty("storeSale");
	if (storeSale==null) storeSale = 100d;
	request.setAttribute("storeSale", storeSale);
	
%>

		<h4 style='cursor:pointer'onclick='renameStore()'>Store name: <%=common.getCharacter().getProperty("storeName")%></h4>
		<c:if test="${storeOpen==true}">
			<a onclick='storeDiabled()'>Click here to shutdown your store</a>
		</c:if>
		<c:if test="${storeOpen==false}">
			<a onclick='storeEnabled()'>Click here to open your store for business</a>
		</c:if>
		<br><br>
		<a onclick='changeStoreSale()' title='Use this feature to set a store-wide sale on all items (or even a store-wide price hike)'>Click here to change your store sale/adjustment value</a>
		<div class='main-splitScreen'>
		<div class='boldbox'><h4>Your Inventory</h4>
		<div id='invItems'>
		<%
			for(CachedEntity item:items)
			{
		
				if (db.checkCharacterHasItemEquipped(common.getCharacter(), item.getKey()))
					continue;
		
				 boolean skip = false;
                for(CachedEntity saleItem:saleItems)
                {
                    if (GameUtils.equals(saleItem.getProperty("itemKey"), item.getKey()))
                    {
                        skip=true;
                        break;
                    }
                }
                if (skip)
                    continue;
				
				out.println(HtmlComponents.generateInvItemHtml(item));
			}
		%>
		</div>
		</div>
		</div>
		
		<div class='main-splitScreen'>
		<div class='boldbox'><h4>Your Storefront</h4>
		<div class='main-item-controls'>
			<a onclick='storeDeleteSoldItemsNew(event)' class='main-item-subnote'>Remove Sold Items</a>
			<a onclick='storeDeleteAllItemsNew(event)' class='main-item-subnote'>REMOVE ALL</a>
		</div>
		<div id='saleItems'>
		<%
			for(CachedEntity saleItem:saleItems)
			{
				if ("Hidden".equals(saleItem.getProperty("status")))
					continue;
				out.println(HtmlComponents.generateSellItemHtml(db,saleItem,request));
			}
		%>
		</div>
		</div>
		</div>
		
