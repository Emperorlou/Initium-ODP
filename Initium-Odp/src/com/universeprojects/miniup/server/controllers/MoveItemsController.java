package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.WebUtils;
import com.universeprojects.miniup.server.services.ContainerService;
import com.universeprojects.miniup.server.services.GroupService;
import com.universeprojects.miniup.server.services.PropertiesService;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

@Controller
public class MoveItemsController extends PageController {

	public MoveItemsController() {
		super("ajax_moveitems");
	}

	@Override
	protected String processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setHeader("Access-Control-Allow-Origin", "*");     // This is absolutely necessary for phonegap to work

		ODPDBAccess db = ODPDBAccess.getInstance(request);
	    CachedDatastoreService ds = db.getDB();
	    CachedEntity character = db.getCurrentCharacter();
	    CachedEntity user = db.getCurrentUser();
	    CachedEntity location = db.getEntity((Key)character.getProperty("locationKey"));
	    
	    // Need to know group status for property info.
	    PropertiesService ps = new PropertiesService(db);
		GroupService gs = new GroupService(db, character);
		Key selfSideKey = null;
		Key otherSideKey = null;
		CachedEntity selfSide = null;
		CachedEntity otherSide = null;
		// used for additional functionality related to group/player houses.
		// Either player owns the house, or is in a group house he's a member of
		boolean isInOwnedHouse = ps.doesUserOwnHouse(location, user) ||
				(gs.characterHasGroup() && ps.doesUserOwnHouse(location, gs.getCharacterGroup()));
		
		// Determine which containers we're going to be using
		String selfSideParam = WebUtils.getStrParam(request, "selfSide");
		String otherSideParam = WebUtils.getStrParam(request, "otherSide");
		String preset = WebUtils.getStrParam(request, "preset");
		if ("location".equals(preset))
		{
			selfSide = character;
			otherSide = location;
			selfSideKey = selfSide.getKey();
			otherSideKey = otherSide.getKey();
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
			// Want to always force character on the left (self) side, so we can ensure 
			// specific commands only show up on one side.
			if(GameUtils.equals(otherSideKey, character.getKey()))
			{
				selfSide = otherSide;
				otherSide = containers.get(0);
				selfSideKey = selfSide.getKey();
				otherSideKey = otherSide.getKey();
			}
		}
		
		// Only allow current character on Self side (for now)
		if(GameUtils.equals(selfSideKey, character.getKey())==false)
			throw new RuntimeException("Hack attempt might have happened.");
		
		// Verify container access.
		ContainerService cs = new ContainerService(db);	
		if (cs.checkContainerAccessAllowed(character, selfSide)==false)
			throw new RuntimeException("Hack attempt might have happened.");
		if (cs.checkContainerAccessAllowed(character, otherSide)==false)
			throw new RuntimeException("Hack attempt might have happened.");

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
		
		// Now fetch all items in these containers...
		List<CachedEntity> selfSideList = db.getItemContentsFor(selfSideKey, false); // Self-side is always character, don't allow otherwise.
		List<CachedEntity> otherSideList = db.getItemContentsFor(otherSideKey, isInOwnedHouse);
		// And sort them... (but only if they're NOT a location)
		if (selfSide.getKind().equals("Location")==false || isInOwnedHouse)	
			selfSideList = db.sortSaleItemList(selfSideList);
		if (otherSide.getKind().equals("Location")==false || isInOwnedHouse)	
			otherSideList = db.sortSaleItemList(otherSideList);
		
		// Handle self side headers here.
		String header = "";
		if (selfSide.getKind().equals("Location"))
		{
			header = "<h5>"+selfSide.getProperty("name")+"</h5>"; 
			if (selfSide.getProperty("banner")!=null)
			{
				header += "<img src='https://initium-resources.appspot.com/"+selfSide.getProperty("banner")+"' border=0 style='width:80%'/>";
			}
		}
		else if (selfSide.getKind().equals("Character"))
		{
			header = GameUtils.renderCharacterWidget(request, db, selfSide, user, true); 
		}
		else if (selfSide.getKind().equals("Item"))
		{
			header = GameUtils.renderItem(selfSide);
		}
		request.setAttribute("selfSideHeader", header);
		
		if (otherSide.getKind().equals("Location"))
		{
			header = "<div class='location-heading-style'><h5>"+otherSide.getProperty("name")+"</h5>";
			if (otherSide.getProperty("banner")!=null)
			{
				header += "<div class='banner-background-image' style='width:80%;background-image:url(\"https://initium-resources.appspot.com"+otherSide.getProperty("banner")+"\")' border=0></div>";
			}
			header += "</div>";
		}
		else if (otherSide.getKind().equals("Character"))
		{
			header = GameUtils.renderCharacterWidget(request, db, otherSide, null, false);
		}
		else if (otherSide.getKind().equals("Item"))
		{
			header = GameUtils.renderItem(otherSide);
		}
		request.setAttribute("otherSideHeader", header);

		String selfKind = selfSide.getKind();
		String otherKind = otherSide.getKind();
		
		// Commands for each side.
		StringBuilder commands = new StringBuilder();
		if (canTransferGold && selfSide.getProperty("dogecoins")!=null)
		{
			commands.append("<div style='height:30px;'>");
			commands.append("<span>");
			commands.append("<img src='https://initium-resources.appspot.com/images/dogecoin-18px.png' border=0/>"+GameUtils.formatNumber(selfSide.getProperty("dogecoins")));
			commands.append("</span>");
			commands.append("<div class='main-item-controls' style='float:right;margin:3px; margin-right:39px;'>");
			commands.append("<a onclick='depositDogecoinsToItem("+otherSide.getId()+", event)'>Deposit gold &xrarr;</a>");
			commands.append("</div>");
			commands.append("</div>");
			request.setAttribute("selfSideCommands", commands.toString());
			// Clear the StringBuilder for the other side commands.
			commands.setLength(0);
		}
		
		if (GameUtils.equals((Boolean)otherSide.getProperty("transmuteEnabled"), true))
			commands.append("<center><a onclick='transmuteItems(event, "+otherSide.getId()+")' class='big-link'>Transmute</a></center><br>");
	
		if (isViewingItem) 
		{
			if(canTransferGold && otherSide.getProperty("dogecoins")!=null)
			{
				commands.append("<div style='height:30px;'>");
				commands.append("<span style='float:right'>");
				commands.append("<img src='https://initium-resources.appspot.com/images/dogecoin-18px.png' border=0/>"+GameUtils.formatNumber(otherSide.getProperty("dogecoins")));
				commands.append("</span>");
				commands.append("<div class='main-item-controls'>");
				commands.append("<a onclick='collectDogecoinsFromItem("+otherSide.getId()+", event)'>&xlarr; Collect gold</a>");
				commands.append("</div>");
				commands.append("</div>");
			}
			
			if (cs.containsEquippable(otherSide, otherSideList))
			{
				commands.append("<div class='main-item-controls' style='display:block;text-align:right'>");
				commands.append("<a onclick='characterEquipSet(event, "+otherSide.getId()+")' title='If you have equipment in this container, this command will automatically unequip everything you have and put it in the container, and then equip your character with the most recently placed equipment in this box.'>Quick auto-equip</a>");
				commands.append("</div>");
			}
		}
		request.setAttribute("otherSideCommands", commands.toString());
		
		// Generate the items themselves.
		List<String> selfItems = new ArrayList<String>();
		List<String> otherItems = new ArrayList<String>();
		// Track categories/strength
		String currentCategory = "";
		for(CachedEntity item:selfSideList)
		{
			String currentItem = "";
			if (db.checkCharacterHasItemEquipped(character, item.getKey()))
				continue;
			
			String itemType = (String)item.getProperty("itemType");
			if (itemType==null) itemType = "";
			
			if (currentCategory.equals(itemType)==false)
			{
				currentItem = "<h3>"+itemType+"</h3>";
				currentCategory = itemType;
			}
			
			currentItem += renderMoveItemHtml(db, item, character, selfSide, otherSide, isViewingItem, true);
			selfItems.add(currentItem);
		}
		request.setAttribute("selfSideItems", selfItems);
		
		boolean disableCategories = "Location".equals(otherSide.getKind()) && isInOwnedHouse == false;
		currentCategory = "";
		for(CachedEntity item:otherSideList)
		{
			String currentItem = "";
			
			String itemType = (String)item.getProperty("itemType");
			if (itemType==null) itemType = "";
			
			if (disableCategories == false && currentCategory.equals(itemType)==false)
			{
				currentItem = "<h3>"+itemType+"</h3>";
				currentCategory = itemType;
			}
			
			currentItem += renderMoveItemHtml(db, item, character, selfSide, otherSide, isViewingItem, false);
			otherItems.add(currentItem);
		}
		request.setAttribute("otherSideItems", otherItems);
		
		return "/WEB-INF/odppages/ajax_moveitems.jsp";
	}

	
	public String renderMoveItemHtml(ODPDBAccess db, CachedEntity item, CachedEntity character, CachedEntity selfSide, CachedEntity otherSide, boolean isViewingItem, boolean isSelfSide)
	{
		StringBuilder curItem = new StringBuilder();
		
		String itemName = "";
		String itemPopupAttribute = "";
		String itemIconElement = "";
		if (item!=null)
		{
			itemName = (String)item.getProperty("name");
			itemPopupAttribute = "class='clue "+GameUtils.determineQuality(item.getProperties())+"' rel='/odp/viewitemmini?itemId="+item.getId()+"'";
			itemIconElement = "<img src='https://initium-resources.appspot.com/"+item.getProperty("icon")+"' border=0/>"; 
		}

		if (CommonChecks.checkItemIsMovable(item))
		{
			if(isSelfSide)
				curItem.append("<a onclick='moveItem(event, "+item.getId()+", \""+otherSide.getKind()+"\", "+otherSide.getId()+")' class='move-left'>--&gt;</a>");
			else
				curItem.append("<a onclick='moveItem(event, "+item.getId()+", \""+selfSide.getKind()+"\", "+selfSide.getId()+")' class='move-right'>&lt;--</a>");
		}
		curItem.append("<div class='main-item'>");
		curItem.append(GameUtils.renderItem(db, character, item));
		curItem.append("<div class='main-item-controls'>");
		if (item.getProperty("maxWeight")!=null)
		{
			// We want to close first when we're already viewing an item container. This makes it so we don't keep "drilling deeper"
			// when we're looking at multiple chests, rather we will be "drilling sideways" i guess
			String closeFirstJs = "";
			if (isViewingItem)
				closeFirstJs = "closePagePopup();";
			curItem.append("<a onclick='"+closeFirstJs+"pagePopup(\"/odp/ajax_moveitems?selfSide="+selfSide.getKind()+"_"+selfSide.getId()+"&otherSide=Item_"+item.getId()+"\")'>Open</a>");
		}
		curItem.append("</div>");
		curItem.append("</div>");
		curItem.append("<br>");
		
		return curItem.toString();
	}
}
