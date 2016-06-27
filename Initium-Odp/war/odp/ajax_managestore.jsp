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
		<%
			for(CachedEntity item:items)
			{
				if (db.checkCharacterHasItemEquipped(common.getCharacter(), item.getKey()))
					continue;
				
				// Check if this item is already being sold. If so, skip it.
				boolean skip = false;
				for(CachedEntity saleItem:saleItems)
				{
					if (((Key)saleItem.getProperty("itemKey")).getId() == item.getKey().getId())
					{
						skip = true;
						continue;
					}
				}
				if (skip)
					continue;
				
				out.println("<div class='main-item'> ");
				out.println("<div class='main-item-container'>");
				out.println(GameUtils.renderItem(item));
				out.println("<br>");
				out.println("		<div ref='itemId'>");
				out.println("			<div class='main-item-controls'>");
				out.println("				<a href='#' onclick='storeSellItemNew("+item.getKey().getId()+")'>Sell This</a>");
				out.println("			</div>");
				out.println("		</div>");
				out.println("	</div>");
				out.println("</div>");
				out.println("<br>");
			}
		%>
		</div>
		</div>
		
		<div class='main-splitScreen'>
		<div class='boldbox'><h4>Your Storefront</h4>
		<div class='main-item-controls'>
			<a onclick='storeDeleteSoldItemsNew()' class='main-item-subnote'>Remove Sold Items</a>
			<a onclick='storeDeleteAllItemsNew()' class='main-item-subnote'>REMOVE ALL</a>
		</div>
		<%
			for(CachedEntity saleItem:saleItems)
			{
				if ("Hidden".equals(saleItem.getProperty("status")))
					continue;
				CachedEntity item = db.getEntity((Key)saleItem.getProperty("itemKey"));
				String itemPopupAttribute = "";
				String itemName = "";
				String itemIconElement = "";
				if (item!=null)
				{
					itemName = (String)item.getProperty("name");
					itemPopupAttribute = "class='clue "+GameUtils.determineQuality(item.getProperties())+"' rel='viewitemmini.jsp?itemId="+item.getKey().getId()+"'";
					itemIconElement = "<img src='"+item.getProperty("icon")+"' border=0/>"; 
				}
				Long cost = (Long)saleItem.getProperty("dogecoins");
				cost=Math.round(cost.doubleValue()*(storeSale/100));
				String finalCost = cost.toString();
				
				out.println("<div class='main-item'>");
				
				out.println(" ");
				out.println("<div class='main-item-container'>");
				out.println("		<div ref='itemId'>");
				String statusText = (String)saleItem.getProperty("status");
				if (statusText.equals("Sold"))
				{
					String soldTo = "";
					if (saleItem.getProperty("soldTo")!=null)
					{
						CachedEntity soldToChar = db.getEntity((Key)saleItem.getProperty("soldTo"));
						if (soldToChar!=null)
							soldTo = " to "+(String)soldToChar.getProperty("name");
					}
					statusText = "<div class='saleItem-sold'>"+statusText+soldTo+"</div>";
				}
				out.println("<a onclick='storeDeleteItemNew("+saleItem.getKey().getId()+")' style='font-size:32px;'>X</a> <a "+itemPopupAttribute+">"+itemIconElement+""+itemName+"</a> <div class='main-item-storefront-status'>(<img src='images/dogecoin-18px.png' class='small-dogecoin-icon' border=0/>"+finalCost+" - "+statusText+")</div>");
				out.println("<br>");
				out.println("<div class='main-item-controls'>");
				out.println("</div>");
				out.println("</div>");
				out.println("</div>");
				out.println("</div>");
				out.println("<br>");
			}
		%>
		</div>
		</div>
