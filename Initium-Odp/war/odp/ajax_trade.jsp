<%@page import="com.universeprojects.miniup.server.HtmlComponents"%>
<%@page import="com.universeprojects.cacheddatastore.CachedEntity"%>
<%@page import="com.universeprojects.miniup.server.GameUtils"%>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@page import="com.universeprojects.cacheddatastore.CachedDatastoreService"%>
<%@page import="com.universeprojects.miniup.server.WebUtils"%>
<%@page import="com.universeprojects.miniup.server.TradeObject"%>
<%@page import="com.google.appengine.api.datastore.Key"%>
<%@page import="java.util.List"%>
<%@page import="com.universeprojects.miniup.server.JspSnippets"%>
<%@page import="com.google.appengine.api.datastore.Entity"%>
<%@page import="com.universeprojects.miniup.server.ODPDBAccess"%>
<% 
    response.setHeader("Access-Control-Allow-Origin", "*");     // This is absolutely necessary for phonegap to work
    ODPDBAccess db = ODPDBAccess.getInstance(request);
    CachedDatastoreService ds = db.getDB(); 
    
    CachedEntity character = db.getCurrentCharacter();
    CachedEntity user = db.getCurrentUser();
            
    String characterMode = (String)character.getProperty("mode");
    
    // Find out if we're actually in a trade right now...
    boolean tradeDoneAlready = false;
    boolean cancelled = false;
    boolean complete = false;
    TradeObject tradeObject = TradeObject.getTradeObjectFor(ds, character);
    if (tradeObject==null || tradeObject.isCancelled())
    {
    	tradeDoneAlready = true;
    	cancelled = true;
    }
    else if (tradeObject.isComplete())
    {
    	tradeDoneAlready = true;
        complete = true;
    }

    
    if (tradeDoneAlready)
    {
    	out.println("<script type='text/javascript'>");
    	out.println("closePagePopup()");
    	if (cancelled)
    		out.println("popupMessage('Trade Cancelled', 'The trade has been cancelled.');");
    	if (complete)
    		out.println("popupMessage('Trade Complete', 'The trade completed successfully');");
    	out.println("</script>");
    	return;
    }
    
    List<CachedEntity> items = db.getFilteredList("Item", "containerKey", character.getKey());
    items = db.sortSaleItemList(items);
    
    List<CachedEntity> saleItems = db.getFilteredList("SaleItem", "characterKey", character.getKey());
    
    
    
    
    
    
    CachedEntity otherCharacter = db.getEntity(tradeObject.getOtherCharacter(character.getKey()));
    request.setAttribute("otherCharacterName", otherCharacter.getProperty("name"));
    
    if (tradeObject.isReady(ds, character))
        request.setAttribute("characterTradeWindowClass", "boldbox boldbox-green");
    else
        request.setAttribute("characterTradeWindowClass", "boldbox");
        
    if (tradeObject.isReady(ds, otherCharacter))
        request.setAttribute("otherCharacterTradeWindowClass", "boldbox boldbox-green");
    else
        request.setAttribute("otherCharacterTradeWindowClass", "boldbox");
    
    request.setAttribute("tradeVersion", tradeObject.getVersion());
    request.setAttribute("chatroomId", "T"+tradeObject.getKey());
    if (GameUtils.equals(character.getProperty("locationKey"), otherCharacter.getProperty("locationKey"))==false)
    {
        db.setTradeCancelled(ds, tradeObject, character);
        out.println("You canot trade with a character who is not in your location.");
        return;
    }
    
    // We need some user entity attributes
    Boolean isPremium = false;
    if (user!=null)
        isPremium = (Boolean)user.getProperty("premium");
    if (isPremium==null) isPremium = false;
    request.setAttribute("isPremium", isPremium);
    
    
    // Get all the characters that this user has (if he is a premium member)
    List<CachedEntity> characterList = null;
    if (isPremium && user!=null)
    {
        characterList = db.getFilteredList("Character", "userKey", user.getKey());
        request.setAttribute("characterList", characterList);
    }
    
    
    
%>
<script type='text/javascript'>
    changeChatTab("PrivateChat");
    setPrivateChatTo("<%=otherCharacter.getProperty("name")%>","<%=otherCharacter.getKey().getId()%>");
</script>
<script type='text/javascript'>var tradeVersion=${tradeVersion};</script>
    <div class='main-page'>
        <h2>Trading with <c:out value="${otherCharacterName}"/></h2> 
        <%=GameUtils.renderCharacterWidget(request, db, character, user, true) %>
        <%=GameUtils.renderCharacterWidget(request, db, otherCharacter, null, false) %>
        
        
        
        
        
        <div class="trade-boxes" style="margin-top: 20px;">
        <div class='main-splitScreen'>
        <div class='${characterTradeWindowClass}'><h4>Your offer</h4>
        <h4><a onclick='tradeSetGoldNew(event,<%=+tradeObject.getDogecoinsFor(character.getKey())%>, "<%=GameUtils.formatNumber(character.getProperty("dogecoins"))%>")'>Gold: <span id='myTradeGoldAmount'><%=GameUtils.formatNumber(tradeObject.getDogecoinsFor(character.getKey())) %></span></a></h4>
        <div id='yourTrade'>
        <%
            for(CachedEntity item:tradeObject.getItemsFor(character.getKey()))
            {
                out.println(HtmlComponents.generatePlayerTradeItemHtml(item));
            }
        %>
        </div>
        </div>
        </div>
        <div class='main-splitScreen'> 
        <div class='${otherCharacterTradeWindowClass}'><h4><c:out value="${otherCharacterName}"/>'s offer</h4>
        <h4>Gold: <%=GameUtils.formatNumber(tradeObject.getDogecoinsFor(otherCharacter.getKey())) %></h4>
        <%
            for(CachedEntity item:tradeObject.getItemsFor(otherCharacter.getKey()))
            {
                out.println(HtmlComponents.generateOtherPlayerTradeItemHtml(item));
            }
        %>
        </div>
        </div>
        </div>
        
        <div class='main-buttonbox' style="margin-top: 20px">
            <a onclick='tradeReadyNew(event)' class='main-button'>Accept Trade</a>
        </div>
        <br>
        <br>
        <div class='main-buttonbox'>
            <a onclick='closePagePopup()' class='main-button'>Cancel</a>
        </div>
        <br>
        <div class='main-splitScreen'>
        <span class="paragraph boldbox-right-link"><a onclick="tradeAddAllItemsNew()" title="This will put the whole inventory into the trade window.">Trade All</a></span>
        <div class='boldbox selection-root'>
        <div class="inventory-main-header">
			<h4>Your Inventory</h4>
	        <div class="main-item-filter">
				<input class="main-item-filter-input" id="filter_invItem" type="text" placeholder="Filter inventory...">
			</div>
			<div class="inventory-main-commands">
				<div class="command-row">
					<label class="command-cell" title="Marks all inventory items for batch operations."><input type="checkbox" class="check-all">Select All</label>
					<a class="command-cell right" title="Adds selected items to the trade window." onclick="selectedItemsTrade(event, '#invItems .invItem')">Trade Selected</a>
				</div>
			</div>
		</div>
        <div id='invItems' class='selection-list'>
        <%
            String currentCategory = "";
            for(CachedEntity item:items)
            {			
                if (db.checkCharacterHasItemEquipped(character, item.getKey()))
                    continue;
				
				if (tradeObject.isItemInTrade(character.getKey(), item.getKey()))
                    continue;
                
                String itemType = (String)item.getProperty("itemType");
                if (itemType==null) itemType = "";
                
                if (currentCategory.equals(itemType)==false)
                {
                    out.println("<h4> "+itemType+"</h4>");
                    currentCategory = itemType;
                }
                
                out.println(HtmlComponents.generateTradeInvItemHtml(item, saleItems, db, ds, request));
            }
        %>
        </div>
        </div>
        </div>
    </div>