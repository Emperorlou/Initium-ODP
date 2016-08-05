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
    ODPDBAccess db = new ODPDBAccess();
    CachedDatastoreService ds = db.getDB();
    
    CachedEntity character = db.getCurrentCharacter(request);
    CachedEntity user = db.getCurrentUser(request);
            
    String characterMode = (String)character.getProperty("mode");
    if (characterMode==null || characterMode.equals(ODPDBAccess.CHARACTER_MODE_TRADING)==false)
    {
        out.println("You are not currently in a trade.");
        return;
    }
    
    // Find out if we're actually in a trade right now...
    TradeObject tradeObject = TradeObject.getTradeObjectFor(ds, character);
    if (tradeObject==null)
    {
        out.println("You are not currently in a trade.");
        return;
    }
    else if (tradeObject.isCancelled())
    {
        out.println("The trade has been cancelled.");
        return;
    }
    else if (tradeObject.isComplete())
    {
        out.println("The trade has completed successfully.");
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
        db.setTradeCancelled(ds, character);
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
    setPrivateChatTo("<%=otherCharacter.getProperty("name")%>");
</script>
<script>var tradeVersion=${tradeVersion};</script>
    <div class='main-page'>
        <h2>Trading with <c:out value="${otherCharacterName}"/></h2> 
        <%=GameUtils.renderCharacterWidget(request, db, character, user, true) %>
        <%=GameUtils.renderCharacterWidget(request, db, otherCharacter, null, false) %>
        
        
        
        
        
        <div class="trade-boxes" style="margin-top: 20px;">
        <div class='main-splitScreen'>
        <div class='${characterTradeWindowClass}'><h4>Your offer</h4>
        <h4><a href='#' onclick='tradeSetGoldNew(event,<%=+tradeObject.getDogecoinsFor(character.getKey())%>)'>Gold: <span id='myTradeGoldAmount'><%=GameUtils.formatNumber(tradeObject.getDogecoinsFor(character.getKey())) %></span></a></h4>
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
                out.println(HtmlComponents.generatePlayerTradeItemHtml(item));
            }
        %>
        </div>
        </div>
        </div>
        
        <div class='main-buttonbox' style="margin-top: 20px">
            <a onclick='tradeReadyNew(event,${tradeVersion})' class='main-button'>Accept Trade</a>
        </div>
        <br>
        <br>
        <div class='main-buttonbox'>
            <a href='#' onclick='closePagePopup()' class='main-button'>Cancel</a>
        </div>
        <br>
        <div class='main-splitScreen'>
        <span class="paragraph boldbox-right-link"><a onclick="tradeAddAllItemsNew()" title="This will put the whole inventory into the trade window.">Trade All</a></span>
        <div class='boldbox'><h4>Your Inventory</h4>
        <div id='invItems'>
        <%
            String currentCategory = "";
            for(CachedEntity item:items)
            {
                if (db.checkCharacterHasItemEquipped(character, item.getKey()))
                    continue;
                
                String itemType = (String)item.getProperty("itemType");
                if (itemType==null) itemType = "";
                
                if (currentCategory.equals(itemType)==false)
                {
                    out.println("<h4> "+itemType+"</h4>");
                    currentCategory = itemType;
                }
                
                if (tradeObject.isItemInTrade(character.getKey(), item.getKey()))
                    continue;
                
                out.println(HtmlComponents.generateTradeInvItemHtml(item, db, ds, request));
            }
        %>
        </div>
        </div>
        </div>
    </div>