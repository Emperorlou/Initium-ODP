<%@page import="com.universeprojects.miniup.CommonChecks"%>
<%@page import="com.universeprojects.miniup.server.DDOSProtectionException"%>
<%@page import="com.universeprojects.miniup.server.services.ContainerService"%>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@page import="com.google.appengine.api.datastore.KeyFactory"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.HashSet"%>
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
	CachedDatastoreService ds = db.getDB();
	try
	{
		auth.doSecurityChecks(request);
	}
	catch(SecurityException e)
	{
		JspSnippets.handleSecurityException_Ajax(e, request, response);
		return;
	}
	
	CommonEntities common = CommonEntities.getInstance(request);
	
	Key selfSideKey = null;
	Key otherSideKey = null;
	CachedEntity selfSide = null;
	CachedEntity otherSide = null;
	
	
	// Determine which containers we're going to be using
	String selfSideParam = WebUtils.getStrParam(request, "selfSide");
	String otherSideParam = WebUtils.getStrParam(request, "otherSide");
	String preset = WebUtils.getStrParam(request, "preset");
	if ("location".equals(preset))
	{
		selfSide = common.getCharacter();
		otherSide = common.getLocation();
		selfSideKey = common.getCharacter().getKey();
		otherSideKey = common.getLocation().getKey();
	}
	else
	{
		String[] parts = selfSideParam.split("_");
		selfSideKey = KeyFactory.createKey(parts[0], Long.parseLong(parts[1]));
		parts = otherSideParam.split("_");
		otherSideKey = KeyFactory.createKey(parts[0], Long.parseLong(parts[1]));
		List<CachedEntity> containers = ds.fetchEntitiesFromKeys(selfSideKey, otherSideKey);
		selfSide = containers.get(0);
		otherSide = containers.get(1);
	}
	
	ContainerService cs = new ContainerService(db);	
	
	if (cs.checkContainerAccessAllowed(common.getCharacter(), selfSide)==false)
		throw new RuntimeException("Hack attempt might have happened.");
	if (cs.checkContainerAccessAllowed(common.getCharacter(), otherSide)==false)
		throw new RuntimeException("Hack attempt might have happened.");
	

	// Now fetch all items in these containers...
	List<CachedEntity> selfSideList = db.getItemContentsFor(selfSideKey);
	List<CachedEntity> otherSideList = db.getItemContentsFor(otherSideKey);
	// And sort them... (but only if they're NOT a location)
	if (selfSide.getKey().getKind().equals("Location")==false)	
		selfSideList = db.sortSaleItemList(selfSideList);
	if (otherSide.getKey().getKind().equals("Location")==false)	
		otherSideList = db.sortSaleItemList(otherSideList);
	
	
	// Populate some jsp values
	request.setAttribute("selfSideName", selfSide.getProperty("name"));
	request.setAttribute("otherSideName", otherSide.getProperty("name"));

	
	// Can we transfer gold too?
	boolean canTransferGold = false;
	boolean isViewingItem = false;
	if (selfSide.getKind().equals("Character"))
	{
		if (otherSide.getKind().equals("Item"))
		{
	canTransferGold = true;
	isViewingItem = true;
		}
	}
	request.setAttribute("isViewingItem", isViewingItem);
%>
<!doctype html>

<html>
<head>

<title>Manage Store - Initium</title>

<style>
.table
{
	display:table;
	width:100%;
}
.header-bar
{
	width:100%;
	display:table-row;
}
.header-cell
{
	display:table-cell;
	width:50%;
	border-bottom:2px dotted #777777;
	vertical-align:bottom;
}
#left
{
	width:50%;
	display:table-cell;
	overflow:hidden;
	border-right:1px dotted #777777;
}
#right
{
	width:50%;
	display:table-cell;
	overflow:hidden;
	border-left:1px dotted #777777;
	padding-left:2%;
} 
.move-left
{
	float:left;
}
.move-right
{
	float:left;
}

.location-heading-style
{
	position:relative;
}
.location-heading-style h5
{
	position: absolute;
    right: 4px;
    bottom: 2px;
    margin: 0px;
}
.location-heading-style .banner-background-image
{
    width: 100%;
    height: 50px;
    background-position: center center;
    background-size: cover;
    border-top-left-radius: 8px;
    border-top-right-radius: 8px;
}
</style>

</head>

<body>
		<div class='page-popup-description'>Transfer items to and from your inventory here with this page.</div>
		<div class='table'>
		<div class='header-bar'>
		<div class='header-cell'>
		<%
			if (selfSide.getKey().getKind().equals("Location"))
			{
				out.println("<h5>"+selfSide.getProperty("name")+"</h5>");
				if (selfSide.getProperty("banner")!=null)
				{
					out.println("<img src='https://initium-resources.appspot.com/"+selfSide.getProperty("banner")+"' border=0 style='width:80%'/>");
				}
			}
			else if (selfSide.getKey().getKind().equals("Character"))
			{
				out.println(GameUtils.renderCharacterWidget(request, db, selfSide, common.getUser(), true)); 
			}
			else if (selfSide.getKey().getKind().equals("Item"))
			{
				out.println(GameUtils.renderItem(selfSide));
			}
		%>
		</div>
		<div class='header-cell' style='text-align:right'>
		<%
			if (otherSide.getKey().getKind().equals("Location"))
			{
				out.println("<div class='location-heading-style'>");
				out.println("<h5>"+otherSide.getProperty("name")+"</h5>");
				if (otherSide.getProperty("banner")!=null)
				{
					out.println("<div class='banner-background-image' style='background-image:url(\"https://initium-resources.appspot.com/"+otherSide.getProperty("banner")+"\")' border=0 style='width:80%'></div>");
				}
				out.println("</div>");
			}
			else if (otherSide.getKey().getKind().equals("Character"))
			{
				out.println(GameUtils.renderCharacterWidget(request, db, otherSide, null, false));
			}
			else if (otherSide.getKey().getKind().equals("Item"))
			{
				out.println(GameUtils.renderItem(otherSide));
			}
		%>
		</div>
		</div>
		<div class='move-items-column normal-container' id='left'>
		<%
			boolean disableCategories = false;
			if (selfSide.getKey().getKind().equals("Location"))
				disableCategories = true;
			
			
			if (canTransferGold && selfSide.getProperty("dogecoins")!=null)
			{
				out.println("<div style='height:30px;'>");
				out.println("<span>");
				out.println("<img src='https://initium-resources.appspot.com/images/dogecoin-18px.png' border=0/>"+GameUtils.formatNumber(selfSide.getProperty("dogecoins")));
				out.println("</span>");
				out.println("<div class='main-item-controls' style='float:right;margin:6px; margin-right:39px;'>");
				out.println("<a onclick='depositDogecoinsToItem("+otherSide.getKey().getId()+", event)'>Deposit&nbsp;gold --></a>");
				out.println("</div>");
				out.println("</div>");
			}
			
			String currentCategory = "";
			for(CachedEntity item:selfSideList)
			{

				// In our own character inventory, we don't want to display items that are currently equipped
				if (GameUtils.equals(selfSide.getKey(), common.getCharacter().getKey()))
				{
					if (db.checkCharacterHasItemEquipped(common.getCharacter(), item.getKey()))
						continue;
				}
				
				boolean hasRequiredStrength = true;
				if (common.getCharacter()!=null)
				{
					Double characterStrength = db.getCharacterStrength(common.getCharacter());
					
					Double strengthRequirement = null;
					try
					{
						strengthRequirement = (Double)item.getProperty("strengthRequirement");
					}
					catch(Exception e)
					{
						// Ignore exceptions
					}
					
					if (strengthRequirement!=null && characterStrength<strengthRequirement)
						hasRequiredStrength = false;
				}
		        String notEnoughStrengthClass = "";
		        if (hasRequiredStrength==false)
		        	notEnoughStrengthClass = "not-enough-strength";
				
				
				String itemType = (String)item.getProperty("itemType");
				if (itemType==null) itemType = "";
				
				if (currentCategory.equals(itemType)==false && disableCategories == false)
				{
					out.println("<h3>"+itemType+"</h3>");
					currentCategory = itemType;
				}
				String itemName = "";
				String itemPopupAttribute = "";
				String itemIconElement = "";
				if (item!=null)
				{
					itemName = (String)item.getProperty("name");
					itemPopupAttribute = "class='clue "+GameUtils.determineQuality(item.getProperties())+"' rel='/odp/viewitemmini?itemId="+item.getKey().getId()+"'";
					itemIconElement = "<img src='https://initium-resources.appspot.com/"+item.getProperty("icon")+"' border=0/>"; 
				}

				if (CommonChecks.checkItemIsMovable(item))
					out.println("<a onclick='moveItem(event, "+item.getKey().getId()+", \""+otherSide.getKind()+"\", "+otherSide.getKey().getId()+")' class='move-left'>--&gt;</a>");
				out.println("<div class='main-item'>");
				out.println(GameUtils.renderItem(db, common.getCharacter(), item));
				out.println("<div class='main-item-controls'>");
				if (item.getProperty("maxWeight")!=null)
				{
					// We want to close first when we're already viewing an item container. This makes it so we don't keep "drilling deeper"
					// when we're looking at multiple chests, rather we will be "drilling sideways" i guess
					String closeFirstJs = "";
					if (isViewingItem)
						closeFirstJs = "closePagePopup();";
					out.println("<a onclick='"+closeFirstJs+"pagePopup(\"ajax_moveitems.jsp?selfSide="+selfSide.getKind()+"_"+selfSide.getId()+"&otherSide=Item_"+item.getId()+"\")'>Open</a>");
				}
				out.println("</div>");
				out.println("</div>");
				out.println("<br>");
			}
		%>
		</div>














		<div class='move-items-column normal-container' id='right'>
		<%
			Boolean transmuteEnabled = (Boolean)otherSide.getProperty("transmuteEnabled");
			if (GameUtils.equals(transmuteEnabled, true))
				out.println("<center><a onclick='transmuteItems(event, "+otherSide.getId()+")' class='big-link'>Transmute</a></center><br>");
			
			
		
			disableCategories = false;
			if (otherSide.getKey().getKind().equals("Location"))
			{
				disableCategories = true;
			}
		
			if (canTransferGold && otherSide.getProperty("dogecoins")!=null)
			{
				out.println("<div style='height:30px;'>");
				out.println("<span style='float:right'>");
				out.println("<img src='https://initium-resources.appspot.com/images/dogecoin-18px.png' border=0/>"+GameUtils.formatNumber(otherSide.getProperty("dogecoins")));
				out.println("</span>");
				out.println("<div class='main-item-controls'>");
				out.println("<a onclick='collectDogecoinsFromItem("+otherSide.getKey().getId()+", event)'><-- Collect&nbsp;gold</a>");
				out.println("</div>");
				out.println("</div>");
			}
			
			if (otherSide.getKey().getKind().equals("Item") && cs.containsEquippable(otherSide, otherSideList))
			{
				out.println("<div class='main-item-controls' style='display:block;text-align:right; float:right;margin-top:10px;'>");
				out.println("<a onclick='characterEquipSet(event, "+otherSide.getKey().getId()+")' title='If you have equipment in this container, this command will automatically unequip everything you have and put it in the container, and then equip your character with the most recently placed equipment in this box.'>Quick auto-equip</a>");
				out.println("</div>");
			}
			
			
			currentCategory = "";
			for(CachedEntity item:otherSideList)
			{
				
				String itemType = (String)item.getProperty("itemType");
				if (itemType==null) itemType = "";
				
				if (currentCategory.equals(itemType)==false && disableCategories == false)
				{
					out.println("<h3>"+itemType+"</h3>");
					currentCategory = itemType;
				}
				String itemName = "";
				String itemPopupAttribute = "";
				String itemIconElement = "";
				if (item!=null)
				{
					itemName = (String)item.getProperty("name");
					itemPopupAttribute = "class='clue "+GameUtils.determineQuality(item.getProperties())+"' rel='/odp/viewitemmini?itemId="+item.getKey().getId()+"'";
					itemIconElement = "<img src='https://initium-resources.appspot.com/"+item.getProperty("icon")+"' border=0/>"; 
				}

				if (CommonChecks.checkItemIsMovable(item))
					out.println("<a onclick='moveItem(event, "+item.getKey().getId()+", \""+selfSide.getKind()+"\", "+selfSide.getKey().getId()+")' class='move-right'>&lt;--</a>");
				out.println("<div class='main-item'>");
				out.println(GameUtils.renderItem(db, common.getCharacter(), item));				
				out.println("<div class='main-item-controls'>");
				if (item.getProperty("maxWeight")!=null)
				{
					// We want to close first when we're already viewing an item container. This makes it so we don't keep "drilling deeper"
					// when we're looking at multiple chests, rather we will be "drilling sideways" i guess
					String closeFirstJs = "";
					if (isViewingItem)
						closeFirstJs = "closePagePopup();";
					out.println("<a onclick='"+closeFirstJs+"pagePopup(\"ajax_moveitems.jsp?selfSide="+selfSide.getKind()+"_"+selfSide.getId()+"&otherSide=Item_"+item.getId()+"\")'>Open</a>");
				}
				out.println("</div>");
				out.println("</div>");
				out.println("<br>");
			}
		%>
		</div>
		</div>
</body>
</html>
