<%@page import="com.universeprojects.cacheddatastore.CachedEntity"%>
<%@page import="com.universeprojects.miniup.server.GameUtils"%>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@page import="com.universeprojects.cacheddatastore.CachedDatastoreService"%>
<%@page import="com.universeprojects.miniup.server.WebUtils"%>
<%@page import="com.universeprojects.miniup.server.TradeObject"%>
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
	CachedDatastoreService ds = db.getDB();
	try
	{
		auth.doSecurityChecks(request);
	}
	catch(SecurityException e)
	{
		return;
	}
	
	CommonEntities common = CommonEntities.getInstance(request);

	
	String characterMode = (String)common.getCharacter().getProperty("mode");
	if (characterMode==null || characterMode.equals(GameFunctions.CHARACTER_MODE_TRADING)==false)
	{
		WebUtils.forceRedirectClientTo("main.jsp", request, response, "The trade has been cancelled.");
		return;
	}
	
	// Find out if we're actually in a trade right now...
	TradeObject tradeObject = TradeObject.getTradeObjectFor(ds, common.getCharacter());
	if (tradeObject==null || tradeObject.isCancelled())
	{
		WebUtils.forceRedirectClientTo("main.jsp", request, response, "The trade has been cancelled.");
		return;
	}
	else if (tradeObject.isComplete())
	{
		WebUtils.forceRedirectClientTo("main.jsp", request, response, "The trade has completed successfully.");
		return;
	}
	
	List<CachedEntity> items = db.getEntityListFrom(common.getCharacter().getKey(), "inventory");
	items = db.sortSaleItemList(items);
	
	List<CachedEntity> saleItems = db.getFilteredList("SaleItem", "characterKey", common.getCharacter().getKey());

	
	
	
	
	
	
	CachedEntity otherCharacter = db.getEntity(tradeObject.getOtherCharacter(common.getCharacter().getKey()));
	request.setAttribute("otherCharacterName", otherCharacter.getProperty("name"));
	
	if (tradeObject.isReady(ds, common.getCharacter()))
		request.setAttribute("characterTradeWindowClass", "boldbox boldbox-green");
	else
		request.setAttribute("characterTradeWindowClass", "boldbox");
		
	if (tradeObject.isReady(ds, otherCharacter))
		request.setAttribute("otherCharacterTradeWindowClass", "boldbox boldbox-green");
	else
		request.setAttribute("otherCharacterTradeWindowClass", "boldbox");
	
	request.setAttribute("tradeVersion", tradeObject.getVersion());

	request.setAttribute("chatroomId", "T"+tradeObject.getKey());


	if (((Key)common.getCharacter().getProperty("locationKey")).getId() != ((Key)otherCharacter.getProperty("locationKey")).getId())
	{
		db.setTradeCancelled(ds, common.getCharacter());
		WebUtils.forceRedirectClientTo("main.jsp", request, response, "You canot trade with a character who is not in your location.");
		return;
	}
	

	// We need some user entity attributes
	Boolean isPremium = false;
	if (common.getUser()!=null)
		isPremium = (Boolean)common.getUser().getProperty("premium");
	if (isPremium==null) isPremium = false;
	request.setAttribute("isPremium", isPremium);
	
	
	// Get all the characters that this user has (if he is a premium member)
	List<CachedEntity> characterList = null;
	if (isPremium && common.getUser()!=null)
	{
		characterList = db.getFilteredList("Character", "userKey", common.getUser().getKey());
		request.setAttribute("characterList", characterList);
	}
	
	
	
%>

<script type='text/javascript'>

	changeChatTab("PrivateChat");
	setPrivateChatTo("<%=otherCharacter.getProperty("name")%>");
</script>

<script>var tradeVersion=${tradeVersion};</script>

	<div class='main-page'>

		<h2>Trading with <c:out value="${otherCharacterName}"/></h2> 
		<%=GameUtils.renderCharacterWidget(request, db, common.getCharacter(), common.getUser(), true) %>
		<%=GameUtils.renderCharacterWidget(request, db, otherCharacter, null, false) %>

		
		
 		
		
		

		
		<div class='main-splitScreen'>
		<div class='${characterTradeWindowClass}'><h4>Your trade</h4>
		<h4><a href='#' onclick='tradeSetGoldNew(<%="event"+tradeObject.getDogecoinsFor(common.getCharacter().getKey())%>)'>Gold: <%=GameUtils.formatNumber(tradeObject.getDogecoinsFor(common.getCharacter().getKey())) %></a></h4>
		<div id='yourTrade'>
		<%
			for(CachedEntity item:tradeObject.getItemsFor(common.getCharacter().getKey()))
			{
				out.println(HtmlComponents.generatePlayerTradeItemHtml(item));
			}
		%>
		</div>
		</div>
		</div>

		<div class='main-splitScreen'> 
		<div class='${otherCharacterTradeWindowClass}'><h4><c:out value="${otherCharacterName}"/>'s trade</h4>
		<h4>Gold: <%=GameUtils.formatNumber(tradeObject.getDogecoinsFor(otherCharacter.getKey())) %></h4>
		<%
			for(CachedEntity item:tradeObject.getItemsFor(otherCharacter.getKey()))
			{
				out.println(HtmlComponents.generatePlayerTradeItemHtml(item));
			}
		%>
		</div>
		</div>

		
		<div class='main-buttonbox'>
			<a onclick='tradeReady(${tradeVersion})' class='main-button'>Accept Trade</a>
		</div>

		<br>
		<br>
		<div class='main-buttonbox'>
			<a href='#' onclick='tradeAddAllItemsNew()' class='main-button'>Trade All</a>
		</div>
		<br>

		<div class='main-splitScreen'>
		<div class='boldbox'><h4>Your Inventory</h4>
		<div id='invItems'>
		<%
			String currentCategory = "";
			for(CachedEntity item:items)
			{
				if (db.checkCharacterHasItemEquipped(common.getCharacter(), item.getKey()))
					continue;
				
				String itemType = (String)item.getProperty("itemType");
				if (itemType==null) itemType = "";
				
				if (currentCategory.equals(itemType)==false)
				{
					out.println("<h4> "+itemType+"</h4>");
					currentCategory = itemType;
				}
				
				if (tradeObject.isItemInTrade(common.getCharacter().getKey(), item.getKey()))
					continue;
				
				out.println(HtmlComponents.generateTradeInvItemHtml(item, db, ds, request));
			}
		%>
		</div>
		</div>
		</div>
	</div>