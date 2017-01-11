<%@page import="com.universeprojects.cacheddatastore.CachedEntity"%>
<%@page import="com.universeprojects.miniup.server.GameUtils"%>
<%@page import="com.google.appengine.api.datastore.KeyFactory"%>
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

	ODPDBAccess db = ODPDBAccess.getInstance(request);
	
	
	
	
	// Get all the items we see here in this location...
	List<CachedEntity> items = db.getFilteredList("Item", "containerKey", db.getCurrentCharacter().getKey());
	items = db.sortSaleItemList(items);
	
	List<CachedEntity> saleItems = db.getFilteredList("SaleItem", "characterKey", db.getCurrentCharacter().getKey());
%>
<html>
<body>
		<div class='boldbox'>
			<div class="inventory-main-header">
				<h4>Your Equipment</h4>
				<div class="inventory-main-commands">
					<div class="command-row">
						<a class="command-cell left" title="Unequips all items currently on your character." onclick="characterUnequipAll(event)">Unequip All</a>&nbsp;
					</div>
				</div>
			</div>
		<%
			for(String slot:ODPDBAccess.EQUIPMENT_SLOTS)
			{
				out.println("<div class='main-item'><span class='equip-slot'>"+slot+": </span>");
				Key itemKey = (Key)db.getCurrentCharacter().getProperty("equipment"+slot);
				CachedEntity item = db.getEntity(itemKey);
				if (item==null)
				{
					out.println("None");
				}
				else
				{
					
					out.println(" ");
					out.println("<div class='main-item-container'>");
					out.println(GameUtils.renderItem(item));
					out.println("<br>");
					out.println("<div class='main-item-controls'>");
					out.println("<a onclick='characterUnequipItem(event, "+item.getKey().getId()+")'>Unequip</a>");
					out.println("</div>");
					out.println("</div>");
					out.println("<br>");
					
				}
				out.println("</div>");
			}
		%>
		</div>
</body>
</html>