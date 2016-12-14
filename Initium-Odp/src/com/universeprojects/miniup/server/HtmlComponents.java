package com.universeprojects.miniup.server;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;

public class HtmlComponents {

	public static String generateTerritoryView(CachedEntity character, CachedEntity territoryOwnerGroup, CachedEntity territory)
	{
		if (territory==null) return "";
		
		String groupName = "<No owner>";
		if (territoryOwnerGroup!=null)
			groupName = (String)territoryOwnerGroup.getProperty("name");
		String groupStatus = (String)character.getProperty("groupStatus");
		boolean isOwningGroupMember = false;
		if (territoryOwnerGroup!=null && 
				GameUtils.equals(territoryOwnerGroup.getKey(),character.getProperty("groupKey")) && 
				("Member".equals(groupStatus) || "Admin".equals(groupStatus)))
			isOwningGroupMember = true;
		boolean isOwningGroupAdmin = false;
		if ("Admin".equals(groupStatus) && isOwningGroupMember)
			isOwningGroupAdmin = true;
		String characterStatus = (String)character.getProperty("status");
		
		
		StringBuilder sb = new StringBuilder();
		sb.append("<div class='boldbox' id='territoryView'>");
		sb.append("<h4>Territory</h4>");
		sb.append("<h5>Owning Group: "+groupName+"</h5>");
		sb.append("<div class='main-item-controls'>");
		sb.append("<a onclick='doTerritoryClaim(event)'>Claim Territory</a>");
		sb.append("<a onclick='doTerritoryRetreat(event)'>Retreat from Territory</a>");
		sb.append("</div>");
		if (isOwningGroupAdmin)
		{
			sb.append("<h5>Admin Controls</h5>");
			sb.append("<div class='main-item-controls'>");
			sb.append("<a onclick='doTerritoryVacate(event)'>Vacate and Surrender Territory</a>");
			sb.append("</div>");
		}
		sb.append("<br>");
		sb.append("<br>");
		sb.append("<div style='text-align:center'>");
		sb.append("<div class='smallbox'>");
		sb.append("1st Line");
//		sb.append("<p><span class='hint' rel='#engagedDefenders'>${defenderEngaged1Count}</span>/<span class='hint' rel='#defenderCount'>${defender1Count}</span></p>");
		if ("Defending1".equals(characterStatus))
			sb.append("<p class='hint' rel='#joinedDefenderGroup'>Joined</p>");
		else
			sb.append("<p><a onclick='doTerritorySetDefense(event, \"1\")'>Join</a></p>");
		sb.append("</div>");
		sb.append("<div class='smallbox'>");
		sb.append("2nd Line");
//		sb.append("<p><span class='hint' rel='#engagedDefenders'>${defenderEngaged2Count}</span>/<span class='hint' rel='#defenderCount'>${defender2Count}</span></p>");
		if ("Defending2".equals(characterStatus))
			sb.append("<p class='hint' rel='#joinedDefenderGroup'>Joined</p>");
		else
			sb.append("<p><a onclick='doTerritorySetDefense(event, \"2\")'>Join</a></p>");
		sb.append("</div>");
		sb.append("<div class='smallbox'>");
		sb.append("3rd Line");
//		sb.append("<p><span class='hint' rel='#engagedDefenders'>${defenderEngaged3Count}</span>/<span class='hint' rel='#defenderCount'>${defender3Count}</span></p>");
		if ("Defending3".equals(characterStatus))
			sb.append("<p class='hint' rel='#joinedDefenderGroup'>Joined</p>");
		else
			sb.append("<p><a onclick='doTerritorySetDefense(event, \"3\")'>Join</a></p>");
		sb.append("</div>");
		sb.append("<div class='smallbox'>");
		sb.append("Not Defending");
//		sb.append("<p>${notDefendingCount}</p>");
		if (characterStatus==null || "Normal".equals(characterStatus))
			sb.append("<p class='hint' rel='#joinedDefenderGroup'>Joined</p>");
		else
			sb.append("<p><a onclick='doTerritorySetDefense(event, \"0\")'>Join</a></p>");
		sb.append("</div>");
		sb.append("</div>");
		sb.append("</div>");
		
		return sb.toString();
	}
	
	public static String generateInvItemHtml(CachedEntity item) {
		
		if (item==null)
			return " ";
		
		String result = "";
			   result+="<div class='invItem' ref="+item.getKey().getId()+">";
			   result+="<div class='main-item'>";
			   result+="<input type=checkbox>";
			   result+="<div class='main-item-container'>";
			   result+=GameUtils.renderItem(item);
			   result+="<br>";
			   result+="			<div class='main-item-controls'>";
			   result+="				<a onclick='storeSellItemNew(event,"+item.getKey().getId()+")'>Sell This</a>";
			   result+="			</div>";
			   result+="		</div>";
			   result+="	</div>";
			   result+="</div>";
			   result+="<br>";
			   
		return result;
	}
	
	public static String generateSellItemHtml(ODPDBAccess db, CachedEntity saleItem, HttpServletRequest request) {
		
		CachedEntity item = db.getEntity((Key)saleItem.getProperty("itemKey"));
		Double storeSale = (Double)db.getCurrentCharacter().getProperty("storeSale");
		if (storeSale == null)
		{
			storeSale = 100.0;
		}
		String itemPopupAttribute = "";
		String itemName = "";
		String itemIconElement = "";
		if (item!=null)
		{
			itemName = (String)item.getProperty("name");
			itemPopupAttribute = "class='clue "+GameUtils.determineQuality(item.getProperties())+"' rel='viewitemmini.jsp?itemId="+item.getKey().getId()+"'";
			itemIconElement = "<img src='https://initium-resources.appspot.com/"+item.getProperty("icon")+"' border=0/>"; 
		}
		Long cost = (Long)saleItem.getProperty("dogecoins");
		cost=Math.round(cost.doubleValue()*(storeSale/100));
		String finalCost = cost.toString();
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
		
		String result = "";
			   result+="<div class='saleItem' ref='"+saleItem.getKey().getId()+"'>";
		   	   result+="<div class='main-item'>";
		   	   result+="<input type='checkbox'>";
		   	   result+=" ";
		   	   result+="<div class='main-item-container'>";
		   	   result+="<a onclick='storeDeleteItemNew(event,"+saleItem.getKey().getId()+")' class='main-item-bigx'>X</a> <a "+itemPopupAttribute+">"+itemIconElement+""+itemName+"</a> <div class='main-item-storefront-status'>(<img src='https://initium-resources.appspot.com/images/dogecoin-18px.png' class='small-dogecoin-icon' border=0/>"+finalCost+" - "+statusText+")</div>";
//		   	   result+="<br>";
//		   	   result+="<div class='main-item-controls'>";
//		   	   result+="</div>";
		   	   result+="</div>";
		   	   result+="</div>";
		   	   result+="</div>";
		   	   
   	   return result;
	}
	
	public static String generateStoreItemHtml(ODPDBAccess db, CachedEntity selfCharacter, CachedEntity storeCharacter, CachedEntity item, CachedEntity saleItem, HttpServletRequest request)
	{

		boolean hasRequiredStrength = true;
		if (selfCharacter!=null)
		{
			Double characterStrength = db.getCharacterStrength(selfCharacter);
			
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
		
		
        String itemName = "(Item Destroyed)";
        String itemPopupAttribute = "";
        String itemIconElement = "";
        Double storeSale = (Double)storeCharacter.getProperty("storeSale");
        if (storeSale==null) storeSale = 100d;
        if (item!=null)
        {
            itemName = (String)item.getProperty("name");
            itemPopupAttribute = "class='clue "+GameUtils.determineQuality(item.getProperties())+" ' rel='viewitemmini.jsp?itemId="+item.getKey().getId()+"'";
            itemIconElement = "<img src='https://initium-resources.appspot.com/"+item.getProperty("icon")+"' border=0/>"; 
        }

        
        
        Long cost = (Long)saleItem.getProperty("dogecoins");
        cost=Math.round(cost.doubleValue()*(storeSale/100));
        String finalCost = GameUtils.formatNumber(cost, false);
        
      
        String result ="";
		result+="<div class='saleItem' ref='"+saleItem.getKey().getId()+"'>";
		result+="<div class='main-item'>";
       	result+="<span><img src='https://initium-resources.appspot.com/images/dogecoin-18px.png' class='small-dogecoin-icon' border=0/>"+finalCost+"</span>";
       	result+="<span class='"+notEnoughStrengthClass+"'>";
       	if ("Selling".equals(saleItem.getProperty("status")))
   	    	result+="<a "+itemPopupAttribute+">"+itemIconElement+""+itemName+"</a> - <a onclick='storeBuyItemNew(event, \""+itemName.replace("'", "`")+"\",\""+finalCost+"\","+storeCharacter.getKey().getId()+","+saleItem.getId()+", "+storeCharacter.getKey().getId()+")'>Buy this</a>";
   	    else if ("Sold".equals(saleItem.getProperty("status")))   
   	    	result+="<a "+itemPopupAttribute+">"+itemIconElement+""+itemName+"</a> - <div class='saleItem-sold'>SOLD</div>";
       	result+="</span>";
    	result+="</div>";
       	result+="</div>";
       	result+="<br>";
		
		return result;
	}

	public static String generateTradeInvItemHtml(CachedEntity item, List<CachedEntity> saleItems, ODPDBAccess db, CachedDatastoreService ds, HttpServletRequest request) {
		
		if (item==null)
			return " ";
		
		Boolean isVending = false;
		String saleText = "";
		// Determine if this item is for sale or not and mark it as such after
		for(CachedEntity saleItem:saleItems)
		{
			if (GameUtils.equals(saleItem.getProperty("itemKey"),item.getKey()))
			{
				saleText = "<div class='main-item-subnote' style='color:#FF0000'> - Selling</div>";
				isVending = true;
				break;
			}
		}
		
		String result = "";
		   result+="<div class='invItem' ref="+item.getKey().getId()+">";
		   result+="<div class='main-item'>";
		   result+="<input type=checkbox>";
		   result+="<div class='main-item-container'>";
		   result+=GameUtils.renderItem(item)+saleText;
		   result+="<br>";
		   if(isVending == false){
		   result+="			<div class='main-item-controls'>";
		   result+="				<a onclick='tradeAddItemNew(event,"+item.getKey().getId()+")'>Add to trade window</a>";
		   result+="			</div>";
		   }
		   result+="		</div>";
		   result+="	</div>";
		   result+="</div>";
		   result+="<br>";
			   
		return result;
	}
	
	public static String generatePlayerTradeItemHtml(CachedEntity item){
		
		String result = "";
			   result+="<div class='tradeItem' ref="+item.getKey().getId()+">";
		       result+="<div class='main-item'>";
		       result+="<div class='main-item-container'>";
		       result+=GameUtils.renderItem(item);
		       result+="<br>";
		       result+="			<div class='main-item-controls'>";
		       result+="				<a onclick='tradeRemoveItemNew(event,"+item.getKey().getId()+")'>Remove</a>";
		       result+="			</div>";
		       result+="		</div>";
		       result+="	</div>";
		       result+="</div>";
		       result+="<br>";
		
		
		return result;
	}
	
public static String generateOtherPlayerTradeItemHtml(CachedEntity item){
		
		String result = "";
			   result+="<div class='tradeItem' ref="+item.getKey().getId()+">";
		       result+="<div class='main-item'>";
		       result+="<div class='main-item-container'>";
		       result+=GameUtils.renderItem(item);
		       result+="<br>";
		       result+="		</div>";
		       result+="	</div>";
		       result+="</div>";
		       result+="<br>";
		
		
		return result;
	}
	
	/**
	 * Generator method to create consistent toggle buttons.
	 * 
	 * @param buttonId
	 * 	The id of the toggle button
	 * @param title
	 * 	Hover text for the link
	 * @param onClick
	 * 	The action to perform when clicked
	 * @param imgSource
	 * 	Image source for the button
	 * @return 
	 * 	Formatted HTML of the generated button
	 */
	private static String generateButtonBarItem(String buttonId, String title, String onClick, String imgSource)
	{
		return "<a id='"+buttonId+"' onclick='"+onClick+"' title='"+title+"'><img src='"+imgSource+"' border=0 /></a>";
	}

	public static String generateManageStoreButton()
	{
		return generateButtonBarItem("manageStore", "Opens your storefront management page so you can setup items for sale", "viewManageStore()", "https://initium-resources.appspot.com/images/ui/manageStore.png");
	}
	
	public static String generateToggleStorefront(CachedEntity character)
	{
		if("MERCHANT".equals(character.getProperty("mode")))
		{
			return generateButtonBarItem("toggleStorefront", "Clicking here will DISABLE your storefront so other players cannot buy your goods.", "storeDisabledNew(event)", "https://initium-resources.appspot.com/images/ui/storefrontEnabled.png");
		}
		else
		{
			return generateButtonBarItem("toggleStorefront", "Clicking here will ENABLE your storefront so other players can buy your goods.", "storeEnabledNew(event)", "https://initium-resources.appspot.com/images/ui/storefrontDisabled.png");
		}
	}
	
	public static String generateTogglePartyJoin(CachedEntity character)
	{
		if("TRUE".equals(character.getProperty("partyJoinsAllowed")))
		{
			return generateButtonBarItem("togglePartyJoin", "Clicking here will DISALLOW other players from joining your party.", "togglePartyJoins(event)", "https://initium-resources.appspot.com/images/ui/partyJoinsAllowed.png");
		}
		else
		{
			return generateButtonBarItem("togglePartyJoin", "Clicking here will ALLOW other players to join your party.", "togglePartyJoins(event)", "https://initium-resources.appspot.com/images/ui/partyJoinsDisallowed.png");
		}
	}
	
	public static String generateToggleDuel(CachedEntity character)
	{
		return generateButtonBarItem("toggleDuel", "Clicking here will ENABLE duel requests. This would allow other players to request a duel with you.", "allowDuelRequests()", "https://initium-resources.appspot.com/images/ui/duelRequestsDisallowed.png");
		/*
		if("TRUE".equals(character.getProperty("duelRequestsAllowed")))
		{
			return generateButtonBarItem("toggleDuel", "Clicking here will DISABLE duel requests. This would prevent other players from requesting a duel with you.", "allowDuelRequests()", "https://initium-resources.appspot.com/images/ui/duelRequestsAllowed.png");
		}
		else
		{
			return generateButtonBarItem("toggleDuel", "Clicking here will ENABLE duel requests. This would allow other players to request a duel with you.", "allowDuelRequests()", "https://initium-resources.appspot.com/images/ui/duelRequestsDisallowed.png");
		}
		*/
	}
	
	public static String generateToggleCloak(CachedEntity character)
	{
		if(Boolean.TRUE.equals(character.getProperty("cloaked")))
		{
			return generateButtonBarItem("toggleCloak", "Clicking here will SHOW your equipment and stats to other players.", "toggleCloaked(event)", "https://initium-resources.appspot.com/images/ui/cloakedEnabled.png");
		}
		else
		{
			return generateButtonBarItem("toggleCloak", "Clicking here will HIDE your equipment and stats from other players.", "toggleCloaked(event)", "https://initium-resources.appspot.com/images/ui/cloakedDisabled.png");
		}
	}
	
	public static String generateButtonBar(CachedEntity character)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<div id='buttonbar-main'>");
		// Buttons
		sb.append("<span>" + generateManageStoreButton() + "</span>");
		sb.append("<span>" + generateToggleStorefront(character) + "</span>");
		sb.append("<span>" + generateTogglePartyJoin(character) + "</span>");
		sb.append("<span>" + generateToggleDuel(character) + "</span>");
		sb.append("<span>" + generateToggleCloak(character) + "</span>");
		// Help text
		sb.append("<div class='hiddenTooltip' id='buttonbar'><h5>The Button Bar</h5>");
		sb.append("<img src='https://initium-resources.appspot.com/images/ui/manageStore.png' border='0' style='float:left; padding:4px;'>");
		sb.append("<p>This button will take you to your storefront management page. This page allows you to setup your storefront by specifying which items you would like to sell to other players and for how much. More help can be found in the storefront page itself.</p>");
		sb.append("<img src='https://initium-resources.appspot.com/images/ui/storefrontEnabled.png' border='0' style='float:left;padding:4px;'>");
		sb.append("<p>This button will turn on and off your storefront. Since you cannot move while vending, you will need to turn off your store before you go off adventuring. This button makes turning your store on and off quick and easy.</p>");
		sb.append("<img src='https://initium-resources.appspot.com/images/ui/partyJoinsAllowed.png' border='0' style='float:left; padding:4px;'>");
		sb.append("<p>This is the party join button. When enabled (without the red cross), other characters will be able to join you in a party. If you are not already in a party then when someone joins you, you will automatically become the party leader. <br>");
		sb.append("More information on parties and how they work can be found in the <a href='odp/mechanics.jsp#parties'>game mechanics page</a>.</p>");
		sb.append("<img src='https://initium-resources.appspot.com/images/ui/duelRequestsAllowed.png' border='0' style='float:left; padding:4px;'>");
		sb.append("<p>This button allows you to control whether or not you are accepting duel requests. When enabled, other players are able to request to duel with you. You will be given the option to accept a duel request or deny it. When you accept, you will be whisked away into a special arena where you and the other player will engage in battle.<br>"); 
		sb.append("More information on the different types of duels and how they work can be found in the <a href='odp/mechanics.jsp#duels'>game mechanics page</a>.</p>");
		sb.append("<img src='https://initium-resources.appspot.com/images/ui/cloakedEnabled.png' border='0' style='float:left; padding:4px;'>");
		sb.append("<p>This button will not allow other players to see your character stats, referral stats, or equipment. It can be an important tool in PvP to hide your equipment so other players are less prepared to attack you since they do not know what you're weak to. However if you're not planning on doing PvP any time soon, keeping this option off makes it easier for people to see what you have and to help you - or just to show off your great gear.</p>");
		sb.append("</div>");
		// Help button
		sb.append("<span class='hint' rel='#buttonbar' style='float:right'><img src='https://initium-resources.appspot.com/images/ui/help.png' border='0'></span>");
		sb.append("</div>");
		return sb.toString();
	}
	
	public static String generateGroupMemberApplication(CachedEntity applied)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<div><div class='main-item-container'>");
		sb.append("<a class='main-item clue' rel='viewcharactermini.jsp?characterId=" + applied.getId() + "'>" + applied.getProperty("name"));
		sb.append("<div class='main-item-controls' style='top:0px'>");
		sb.append("<a onclick='groupAcceptJoinGroupApplication(event, " + applied.getId() + ")'>Accept</a>");
		sb.append("<a onclick='groupDenyJoinGroupApplication(event, " + applied.getId() + ")'>Deny</a>");
		sb.append("</div></a></div></div>");
		return sb.toString();
	}
	
	public static String generateGroupMember(CachedEntity viewingChar, CachedEntity character, CachedEntity group, boolean inGroup, boolean canDeleteGroup)
	{
		String viewStatus = (String)viewingChar.getProperty("groupStatus");
		boolean viewAdmin = inGroup && "Admin".equals(viewStatus);
		boolean viewCreator = inGroup && GameUtils.equals((Key)group.getProperty("creatorKey"), viewingChar.getKey());

		String groupStatus = (String)character.getProperty("groupStatus");
		boolean isAdmin = "Admin".equals(groupStatus);
		boolean isCreator = GameUtils.equals((Key)group.getProperty("creatorKey"), character.getKey());
		String groupPermissionTag = "";
		if (isAdmin)
			groupPermissionTag = "(Admin)";
		if (isCreator)
			groupPermissionTag = "(Creator)";

		String groupRank = "";
		if (character.getProperty("groupRank") != null)
			groupRank = (String) character.getProperty("groupRank");

		StringBuilder sb = new StringBuilder();
		sb.append("<div>");
		sb.append("<div class='main-item-container'>");
		sb.append("<div class='main-item clue' rel='viewcharactermini.jsp?characterId=" + character.getKey().getId() + "'>" + character.getProperty("name"));
		sb.append("</div>");
		sb.append("<div class='main-item-controls' style='top:0px; display:block; margin-bottom:25px;'>");
		sb.append("<span>" + groupPermissionTag + "</span> ");
		
		if (groupStatus.equals("Kicked") == false) {
			sb.append("Position: " + groupRank + "<br>");
			if (character.getProperty("groupLeaveDate") != null)
				sb.append("(This member is leaving the group. They will be out of the group in: "
						+ GameUtils.getTimePassedShortString((Date)character.getProperty("groupLeaveDate"))
						+ ")<br>");
		} 
		else
			sb.append("(Member is being kicked from the group. They will be out of the group in: "
					+ GameUtils.getTimePassedShortString((Date)character.getProperty("leaveGroupDate"))
					+ ")<br>");
		
		if (viewAdmin || viewCreator) 
		{
			sb.append("<a href='#' onclick='setGroupMemberRank(event, \""+ character.getProperty("groupRank") + "\", "+ character.getKey().getId()+ ")'>Set position</a>");
			if (viewCreator) 
			{
				if (isAdmin == false)
					sb.append("<a href='#' onclick='promoteToAdmin(event, "+ character.getKey().getId()+ ")'>Promote to admin</a>");
				else 
				{
					if (!isCreator)
						sb.append("<a href='#' onclick='makeGroupCreator(event, " + character.getKey().getId() + ")' title='Setting this member to group creator will permanently make him in charge of adding and removing admins'>Promote to group creator</a>");
					
					sb.append("<a href='#' onclick='demoteFromAdmin(event, " + character.getKey().getId() + ")'>Demote from admin</a>");
				}
			}
			
			if (groupStatus.equals("Kicked") == false)
				sb.append("<a onclick='groupMemberKick(event, " + character.getKey().getId() + ")'>Kick</a>");
			else
				sb.append("<a onclick='groupMemberKickCancel(" + character.getKey().getId() + ")'>Cancel Kick</a>");
			
			if (viewCreator && canDeleteGroup)
				sb.append("<a onclick='deleteGroup(event)'>Delete group</a>");
		}
		sb.append("</div></div></div>");
		return sb.toString();
	}
	
	public static String generateGroupMergeApplication(CachedEntity group)
	{
		// This only gets generated within the admin block, so we know the viewing user has access.
		StringBuilder sb = new StringBuilder();
		sb.append("<div class='group-container' ref='" + group.getId() + "'>");
		sb.append("<div class='main-item-container'><div class='main-item'>");
		sb.append("<a onclick='viewGroup("+group.getId()+")'>"+group.getProperty("name")+"</a>");
		sb.append("</div><div class='main-item-controls'>");
		sb.append("<a onclick='groupMergeDenyApplication(event, "+group.getId()+")'>Deny Merge</a>");
		sb.append("<a onclick='groupMergeAcceptApplication(event, "+group.getId()+")'>Accept Merge</a>");
		sb.append("</div></div></div>");
		return sb.toString();
	}
}
