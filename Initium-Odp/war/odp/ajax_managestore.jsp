<%@page import="com.universeprojects.miniup.server.WebUtils"%>
<%@page import="com.universeprojects.miniup.server.DDOSProtectionException"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.HashMap"%>
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
	Authenticator auth = null;
	try
	{
		auth = Authenticator.getInstance(request, true);
	}
	catch(DDOSProtectionException e)
	{
		// Ignore this here
	}

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
	
	List<CachedEntity> sortedSaleItems = new ArrayList<CachedEntity>();
	
	boolean storeOpen = false;
	if ("MERCHANT".equals(common.getCharacter().getProperty("mode")))
	{
		storeOpen = true;
	}
	request.setAttribute("storeOpen", storeOpen);
		
	
	Double storeSale = (Double)common.getCharacter().getProperty("storeSale");
	if (storeSale==null) storeSale = 100d;
	request.setAttribute("storeSale", storeSale);
	String storeName = (String)common.getCharacter().getProperty("storeName");
	if(storeName != null) storeName = storeName.replaceAll("'","\\\\'");
	request.setAttribute("storeName", storeName);
	
	
	List<CachedEntity> buyOrders = db.getFilteredList("BuyItem", "characterKey", common.getCharacter().getKey());
	List<String> buyOrdersFormatted = new ArrayList<String>();
	for(CachedEntity buyOrder:buyOrders)
	{
		buyOrdersFormatted.add(HtmlComponents.generateManageStoreBuyOrderHtml(db, buyOrder, request));
	}
	
	request.setAttribute("buyOrders", buyOrdersFormatted);
	request.setAttribute("hasBuyOrders", !buyOrdersFormatted.isEmpty());
%>

		<div class='main-splitScreen'>
		<div class='boldbox'>
		<div style="cursor:pointer" onclick="storeRenameNew(event, '${storeName}')">Store name: <%=common.getCharacter().getProperty("storeName")%></div>
		<br><br>
		<div>Store-wide price is currently: ${storeSale}%</div>
		<div>
		<a onclick='storeSetSaleNew(event)' title='Use this feature to set a store-wide sale on all items (or even a store-wide price hike)'>Click here to change your store sale/adjustment value</a>
		</div>
		</div>
		<div class='boldbox selection-root'>
			<div class="inventory-main-header">
				<h4>Your Inventory</h4>
				<div class="main-item-filter">
					<input class="main-item-filter-input" id="filter_invItem" type="text" placeholder="Filter inventory...">
				</div>
				<div class="inventory-main-commands">
					<div class="command-row">
						<label class="command-cell" title="Marks all inventory items for batch operations."><input type="checkbox" class="check-all">Select All</label>
						<a class="command-cell right" title="Adds the selected inventory items to your storefront for the SAME specified price" onclick="selectedItemsSell(event, '#invItems .invItem')">Sell Selected</a>
					</div>
				</div>
			</div>
			<div id="invItems" class="selection-list">
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
                    	sortedSaleItems.add(saleItem);
                        skip=true;
                        break;
                    }
                }
                if (skip)
                    continue;	
				
				out.println(HtmlComponents.generateInvItemHtml(item));
			}
			// This should remove our sorted items, meaning only sold items remain.
			saleItems.removeAll(sortedSaleItems);
		%>
			</div>
		</div>
		</div>
		
		<div class='main-splitScreen'>
			<div class='boldbox tab-row'>
				<div id='sellorders-tab' class='tab-row-tab tab-selected manageStoreTabs' style='width:auto; height:auto;' onclick='changeGenericTab(event, "manageStoreTabs")'>Sell Orders</div>
				<div id='buyorders-tab' class='tab-row-tab manageStoreTabs' style='width:auto; height:auto;' onclick='changeGenericTab(event, "manageStoreTabs")'>Buy Orders</div>
			</div>
			
			<div class='boldbox selection-root buyorders-content manageStoreTabs tab-content'>
			<div class='inventory-main-header'>
				<h4>Your Buy Orders</h4>
				<div class='inventory-main-commands'>
					<div class='command-row'>
						<label class='command-cell' title='Adds a new buy order to your store.'><a onclick='storeNewBuyOrder(event)'>New Buy Order</a></label>
					</div>
				</div>
			</div>
		
			
			<div id='buyOrders' class='selection-list'>
			<c:forEach var="order" items="${buyOrders}">
				${order}
			</c:forEach>
			<c:if test='${hasBuyOrders==false }'>
				You have not setup any buy orders.
			</c:if>
			</div>
		</div>		
		
		<div class='boldbox selection-root sellorders-content manageStoreTabs tab-content-selected'>
			<div class='inventory-main-header'>
				<h4>Your Storefront</h4>
				<div class='main-item-filter'>
					<input class='main-item-filter-input' id='filter_saleItem' type='text' placeholder='Filter store...'>
				</div>
				<div class='inventory-main-commands'>
					<div class='command-row'>
						<label class='command-cell' title='Marks sold storefront items for batch operations.'><input type='checkbox' <% if (saleItems.size() == 0) { %>disabled<% } %> class='check-group' ref='soldItems'>Select Sold</label>
					</div>
					<div class='command-row'>
						<label class='command-cell' title='Marks all storefront items for batch operations.'><input type='checkbox' class='check-all'>Select All</label>
						<a class='command-cell right' title='Removes the selected sale items from your storefront' onclick='selectedItemsRemoveFromStore(event, "#saleItems .saleItem")'>Remove Selected</a>
					</div>
				</div>
			</div>
		
			
			<div id='saleItems' class='selection-list'>
		<%
			
			if(saleItems.size() > 0)
			{
				%>
				<div class='soldItems-header'>
					<a id='soldItems-minimize' title='Click to show/hide sold items.' onclick='toggleMinimizeSoldItems()'>Sold Items</a>
				</div>
				<div id='soldItems' class='selection-group'>
					<%
					for(CachedEntity saleItem:saleItems)
					{
						if ("Hidden".equals(saleItem.getProperty("status")))
							continue;
						out.println(HtmlComponents.generateSellItemHtml(db,saleItem,request));
					}
					%>
					<hr class='items-separator' />
				</div>
				<%
			}
			for(CachedEntity saleItem:sortedSaleItems)
			{
				if ("Hidden".equals(saleItem.getProperty("status")))
					continue;
				out.println(HtmlComponents.generateSellItemHtml(db,saleItem,request));
			}
		%>
			</div>
		</div>
		</div>