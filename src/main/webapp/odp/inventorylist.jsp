<%@page import="com.universeprojects.cacheddatastore.QueryHelper"%>
<%@page import="com.universeprojects.miniup.CommonChecks"%>
<%@page import="com.universeprojects.miniup.server.DDOSProtectionException"%>
<%@page import="com.universeprojects.miniup.server.WebUtils"%>
<%@page import="com.universeprojects.cacheddatastore.CachedEntity"%>
<%@page import="com.universeprojects.miniup.server.GameUtils"%>
<%@page import="com.google.appengine.api.datastore.KeyFactory"%>
<%@page import="com.google.appengine.api.datastore.Key"%>
<%@page import="java.util.List"%>
<%@page import="com.universeprojects.miniup.server.JspSnippets"%>
<%@page import="com.google.appengine.api.datastore.Entity"%>
<%@page import="com.universeprojects.miniup.server.ODPDBAccess"%>
<%
	response.setHeader("Access-Control-Allow-Origin", "*");		// This is absolutely necessary for phonegap to work

	ODPDBAccess db = ODPDBAccess.getInstance(request);
	
	QueryHelper query = new QueryHelper(db.getDB());
	// Get all the items we see here in this location...
	List<CachedEntity> items = query.getFilteredList("Item", "containerKey", db.getCurrentCharacterKey());
	items = db.sortSaleItemList(items);
	
	List<CachedEntity> saleItems = db.getFilteredList("SaleItem", "characterKey", db.getCurrentCharacterKey());
	
	List<CachedEntity> carryingChars = db.getFilteredList("Character", "locationKey", db.getCurrentCharacterKey());
	if (carryingChars.isEmpty()==false)
		request.setAttribute("isCarryingCharacters", true);
	
%>
<html>
<body>
	<div class='boldbox selection-root'>
		<div class="inventory-main-header">
			<span class='paragraph boldbox-right-link'><a onclick='dropAllInventory(event)' title='This will drop everything in your inventory onto the ground. Equipped and vending items will NOT be dropped.'>Drop All</a></span>
			<h4>Your Inventory</h4>
			<div class="main-item-filter">
				<input class="main-item-filter-input" id="filter_invItem" type="text" placeholder="Filter inventory...">
			</div>
			<div class="inventory-main-commands">
				<div class="command-row">
					<label class="command-cell" title="Marks all inventory items for batch operations."><input type="checkbox" class="check-all">Select All</label>
					<a class="command-cell right" title="Drops any items you've selected in your inventory on the ground." onclick="selectedItemsDrop(event, '#invItems .invItem')">Drop Selected</a>
				</div>
				<div class="command-row">
					<a class="command-cell" title="Merge the selected items." onclick="mergeItemStacks(event, '#invItems .invItem')">Merge Items</a>
					<a class="command-cell right" title="Split the slected item." onclick="splitItemStack(event, '#invItems .invItem')">Split Item</a>
				</div>
				<div class="command-row">
					<a class="command-cell" title="Select 2 containers and click this link to quickly swap the contents of one container for the other. One container must be empty." onclick="swapContainers(event, '#invItems .invItem')">Swap Containers</a>
				</div>
			</div>
		</div>
		<div id="invItems" class="selection-list">
			
		
		<%
			if (carryingChars.isEmpty()==false)
			{
				out.println("<h4>Characters</h4>");
				for(CachedEntity c:carryingChars)
				{
					out.println("<div class='main-item-container'>");
					out.println(GameUtils.renderCharacter(null, c, true, false));
					out.println("<br>");
					out.println("<div class='main-item-controls'>");
					out.println("<a onclick='characterDropCharacter(event, " + c.getId() + ")'>Put on ground</a>");
					out.println("</div>");
					out.println("</div>");
				}
			}
		
		
			String currentCategory = "";
			for(CachedEntity item:items)
			{
				if (db.checkCharacterHasItemEquipped(db.getCurrentCharacter(), item.getKey()))
					continue;
				
				String itemType = (String)item.getProperty("itemType");
				if (itemType==null) itemType = "";
				
				if (currentCategory.equals(itemType)==false)
				{
					out.println("<h4> "+itemType+"</h4>");
					currentCategory = itemType;
					if (currentCategory==null) currentCategory = "";
				}
				
				String saleText = "";
				// Determine if this item is for sale or not and mark it as such after
				for(CachedEntity saleItem:saleItems)
				{
					if (((Key)saleItem.getProperty("itemKey")).getId()==item.getKey().getId())
					{
						saleText = "<div class='main-item-subnote' style='color:#FF0000'> - Selling</div>";
						break;
					}
				}
				
				out.println("<div class='invItem' ref=" + item.getKey().getId() + ">");
				out.println("<div class='main-item'>");
				out.println("<input type=checkbox><div class='main-item-container'>");
				out.println("		"+GameUtils.renderItem(db, db.getCurrentCharacter(), item)+saleText);
				out.println("<br>");
				out.println("		<div class='main-item-controls'>");
				// Get all the slots this item can be equipped in
				if(CommonChecks.checkItemIsEquippable(item))
					out.println("			<a onclick='characterEquipItem(event, " + item.getId() + ")'>Equip</a>");
				
				out.println("			<a onclick='characterDropItem(event, " + item.getId() +")'>Drop on ground</a>");
				if (item.getProperty("maxWeight")!=null)
				{
					out.println("<a onclick='pagePopup(\"/ajax_moveitems.jsp?selfSide=Character_"+db.getCurrentCharacterKey().getId()+"&otherSide=Item_"+item.getKey().getId()+"\")'>Open</a>");
				}
				out.println("		</div>");
				out.println("	</div>");
				out.println("</div>");
				out.println("</div>");
			}
		%>
		</div>
	</div>
</body>
</html>