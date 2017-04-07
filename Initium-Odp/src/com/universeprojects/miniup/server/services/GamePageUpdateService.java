package com.universeprojects.miniup.server.services;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.OperationBase;

public class GamePageUpdateService extends MainPageUpdateService {

	public GamePageUpdateService(ODPDBAccess db, CachedEntity user,
			CachedEntity character, CachedEntity location,
			OperationBase operation) {
		super(db, user, character, location, operation);
	}
/*
 *		<div class='main-splitScreen'>
			<div id='main-merchantlist'>
				<div class='main-button-half' onclick='loadLocationMerchants()' shortcut='83'>
 					<span class='shortcut-key'> (S)</span><img src='https://initium-resources.appspot.com/images/ui/magnifying-glass.png' border=0/> Nearby stores
				</div>
			</div>
		</div>
		<div id='partyPanel' class='main-splitScreen'>
		${partyPanel}
		</div>
		<div></div>
		<div class='main-splitScreen'>
			<div id='main-itemlist'>
				<div class='main-button-half' onclick='loadLocationItems()' shortcut='86'>
 					<span class='shortcut-key'> (V)</span><img src='https://initium-resources.appspot.com/images/ui/magnifying-glass.png' border=0/> Nearby items
				</div>
			</div>
		</div>
		<div class='main-splitScreen'>
			<div id='main-characterlist'>
				<div class='main-button-half' onclick='loadLocationCharacters()' shortcut='66'>
 					<span class='shortcut-key'> (B)</span><img src='https://initium-resources.appspot.com/images/ui/magnifying-glass.png' border=0/> Nearby characters
				</div>
			</div>
		</div>
 *
 */
	@Override
	protected String updateButtonList_NormalMode()
	{
		StringBuilder newHtml = new StringBuilder();
		
		newHtml.append("<div class='titlebar'>ACTIONS</div>");
		newHtml.append("<div class='backdrop2a navigationbox'>");
		newHtml.append("	<div class='titlebar'>This location</div>");
		newHtml.append("	<a href='/main.jsp' class='button2' title='This will take you back to the old UI.'>Old UI</a>");
		newHtml.append("	<a href='#' class='button2' shortcut='69' onclick='doExplore(false)' title='Explore this location for new paths or monsters.'><span class='shortcut-key'>(E)</span>Explore</a>");
		newHtml.append("	<a href='#' class='button2' shortcut='87' onclick='doExplore(true)' title='This button allows you to explore while ignoring combat sites. The shortcut key for this is W.'><span class='shortcut-key'>(W)</span>Explore <img src='https://initium-resources.appspot.com/images/ui/ignore-combat-sites.png' border=0/></a>");
		newHtml.append("	<a href='#' class='button2' shortcut='83' onclick='loadLocationMerchants()' title='Shows all players that are currently vending in your location.'><span class='shortcut-key'>(S)</span>Nearby Merchants</a>");
		newHtml.append("	<a href='#' class='button2' shortcut='86' onclick='loadLocationItems()' title='Shows all items that are on the ground in your location.'><span class='shortcut-key'>(V)</span>Nearby Items</a>");
		newHtml.append("	<a href='#' class='button2' shortcut='66' onclick='loadLocationCharacters()' title='Shows all characters that are with you in your location.'><span class='shortcut-key'>(B)</span>Nearby Characters</a>");
		if ("CampSite".equals(location.getProperty("type")))
		{
			newHtml.append("<a onclick='campsiteDefend()' class='button2' shortcut='68' onclick='popupPermanentOverlay(\"Defending...\", \"You are looking out for threats to your camp.\")'><span class='shortcut-key'>(D)</span>Defend</a>");
		}
		if ("RestSite".equals(location.getProperty("type")) || "CampSite".equals(location.getProperty("type")))
		{
			newHtml.append("<a href='#' class='button2' shortcut='82' onclick='doRest()'><span class='shortcut-key'>(R)</span>Rest</a>");
		}
		
		if (location.getProperty("name").toString().equals("Aera Inn"))
		{
			newHtml.append("<a class='button2' onclick='doDrinkBeer(event)'>Drink Beer</a>");
		}
		
		if (getGroup()!=null && location.getProperty("ownerKey")!=null && ((Key)location.getProperty("ownerKey")).getId() == user.getKey().getId())
		{
			newHtml.append("<a href='#' class='button2' onclick='giveHouseToGroup()'>Give this property to your group</a>");
		}
		
		if (db.isCharacterAbleToCreateCampsite(db.getDB(), character, location))
		{
			newHtml.append("<a href='#' class='button2' onclick='createCampsite()'>Create a campsite here</a>");
		}
		
		if ("CityHall".equals(location.getProperty("type")))
		{
			newHtml.append("<a href='#' class='button2' onclick='buyHouse(event)'>Buy a new house</a>");
		}
		
		if (user!=null && GameUtils.equals(location.getProperty("ownerKey"), user.getKey()))
		{
			newHtml.append("<a onclick='renamePlayerHouse(event)' class='button2'>Rename this house</a>");
		}
		newHtml.append("</div>");
		

		List<Integer> permanentPaths = new ArrayList<Integer>();
		List<Integer> propertyPaths = new ArrayList<Integer>();
		List<Integer> combatSitePaths = new ArrayList<Integer>();
		List<Integer> otherPaths = new ArrayList<Integer>();
		for(int i = 0; i<paths.size(); i++)
		{
			if (paths.get(i).getProperty("ownerKey")!=null || destLocations.get(i).getProperty("ownerKey")!=null)
				propertyPaths.add(i);
			else if ("Permanent".equals(destLocations.get(i).getProperty("type")))
				permanentPaths.add(i);
			else if ("CombatSite".equals(destLocations.get(i).getProperty("type")))
				combatSitePaths.add(i);
			else
				otherPaths.add(i);
		}

		
		int shortcutStart = 49;
		int shortcutNumber = 1;
		int forgettableCombatSites = 0;
		StringBuilder forgettableCombatSiteList = new StringBuilder();
		forgettableCombatSiteList.append("\"");
		
		
		if (permanentPaths.isEmpty()==false)
		{
			newHtml.append("<div class='backdrop2a navigationbox'>");
			newHtml.append("	<div class='titlebar'>Exits</div>");
			for(int i = 0; i<permanentPaths.size(); i++)
			{
				CachedEntity path = paths.get(permanentPaths.get(i));
				CachedEntity destLocation = destLocations.get(permanentPaths.get(i));
				Integer pathEnd = pathEnds.get(permanentPaths.get(i));
				
				Long travelTime = (Long)path.getProperty("travelTime");
				
	
				String shortcutPart = "";
				String shortcutKeyIndicatorPart = "";
				if (shortcutNumber<10)
				{
					shortcutPart = "shortcut='"+(shortcutStart+shortcutNumber-1)+"'";
					shortcutKeyIndicatorPart = "<span class='shortcut-key' title='This indicates the keyboard shortcut to use to activate this button'>("+shortcutNumber+")</span>";
					shortcutNumber++;
				}
	
				String destLocationName = (String)destLocation.getProperty("name");
				
				String buttonCaption = ""+destLocationName;
				String buttonCaptionOverride = (String)path.getProperty("location"+pathEnd+"ButtonNameOverride");
				if (buttonCaptionOverride!=null && buttonCaptionOverride.trim().equals("")==false)
					buttonCaption = buttonCaptionOverride;
				
				
				
				newHtml.append("<a href='#' onclick='doGoto(event, "+path.getKey().getId()+", "+travelTime+")' class='button2' "+shortcutPart+" >"+shortcutKeyIndicatorPart+buttonCaption+"</a>");
				
			}
			newHtml.append("</div>");
		}
		
		
		if (propertyPaths.isEmpty()==false)
		{
			newHtml.append("<div class='backdrop2a navigationbox'>");
			newHtml.append("	<div class='titlebar'>Properties</div>");
			for(int i = 0; i<propertyPaths.size(); i++)
			{
				CachedEntity path = paths.get(propertyPaths.get(i));
				CachedEntity destLocation = destLocations.get(propertyPaths.get(i));
				Integer pathEnd = pathEnds.get(propertyPaths.get(i));
				
				Long travelTime = (Long)path.getProperty("travelTime");
				
	
				String shortcutPart = "";
				String shortcutKeyIndicatorPart = "";
				if (shortcutNumber<10)
				{
					shortcutPart = "shortcut='"+(shortcutStart+shortcutNumber-1)+"'";
					shortcutKeyIndicatorPart = "<span class='shortcut-key' title='This indicates the keyboard shortcut to use to activate this button'>("+shortcutNumber+")</span>";
					shortcutNumber++;
				}
	
				String destLocationName = (String)destLocation.getProperty("name");
				
				String buttonCaption = ""+destLocationName;
				String buttonCaptionOverride = (String)path.getProperty("location"+pathEnd+"ButtonNameOverride");
				if (buttonCaptionOverride!=null && buttonCaptionOverride.trim().equals("")==false)
					buttonCaption = buttonCaptionOverride;
				
				
				
				newHtml.append("<a href='#' onclick='doGoto(event, "+path.getKey().getId()+", "+travelTime+")' class='button2' "+shortcutPart+" >"+shortcutKeyIndicatorPart+buttonCaption+"</a>");
				
			}
			newHtml.append("</div>");
		}
		
		
		
		if (combatSitePaths.isEmpty()==false)
		{
			newHtml.append("<div class='backdrop2a navigationbox'>");
			newHtml.append("	<div class='titlebar'>Combat Sites</div>");
			for(int i = 0; i<combatSitePaths.size(); i++)
			{
				CachedEntity path = paths.get(combatSitePaths.get(i));
				CachedEntity destLocation = destLocations.get(combatSitePaths.get(i));
				Integer pathEnd = pathEnds.get(combatSitePaths.get(i));
				
				Long travelTime = (Long)path.getProperty("travelTime");
				
	
				String shortcutPart = "";
				String shortcutKeyIndicatorPart = "";
				if (shortcutNumber<10)
				{
					shortcutPart = "shortcut='"+(shortcutStart+shortcutNumber-1)+"'";
					shortcutKeyIndicatorPart = "<span class='shortcut-key' title='This indicates the keyboard shortcut to use to activate this button'>("+shortcutNumber+")</span>";
					shortcutNumber++;
				}
	
				String destLocationName = (String)destLocation.getProperty("name");
				
				String buttonCaption = ""+destLocationName;
				String buttonCaptionOverride = (String)path.getProperty("location"+pathEnd+"ButtonNameOverride");
				if (buttonCaptionOverride!=null && buttonCaptionOverride.trim().equals("")==false)
					buttonCaption = buttonCaptionOverride;
				
				
				
				newHtml.append("<a href='#' onclick='doGoto(event, "+path.getKey().getId()+", "+travelTime+")' class='button2' "+shortcutPart+" >"+shortcutKeyIndicatorPart+buttonCaption+"</a>");
				
			}
			newHtml.append("</div>");
		}
		
		if (otherPaths.isEmpty()==false)
		{
			newHtml.append("<div class='backdrop2a navigationbox'>");
			newHtml.append("	<div class='titlebar'>More Locations</div>");
			for(int i = 0; i<otherPaths.size(); i++)
			{
				CachedEntity path = paths.get(otherPaths.get(i));
				CachedEntity destLocation = destLocations.get(otherPaths.get(i));
				Integer pathEnd = pathEnds.get(otherPaths.get(i));
				
				Long travelTime = (Long)path.getProperty("travelTime");
				
	
				String shortcutPart = "";
				String shortcutKeyIndicatorPart = "";
				if (shortcutNumber<10)
				{
					shortcutPart = "shortcut='"+(shortcutStart+shortcutNumber-1)+"'";
					shortcutKeyIndicatorPart = "<span class='shortcut-key' title='This indicates the keyboard shortcut to use to activate this button'>("+shortcutNumber+")</span>";
					shortcutNumber++;
				}
	
				String destLocationName = (String)destLocation.getProperty("name");
				
				String buttonCaption = ""+destLocationName;
				String buttonCaptionOverride = (String)path.getProperty("location"+pathEnd+"ButtonNameOverride");
				if (buttonCaptionOverride!=null && buttonCaptionOverride.trim().equals("")==false)
					buttonCaption = buttonCaptionOverride;
				
				
				
				newHtml.append("<a href='#' onclick='doGoto(event, "+path.getKey().getId()+", "+travelTime+")' class='button2' "+shortcutPart+" >"+shortcutKeyIndicatorPart+buttonCaption+"</a>");
				
			}
			newHtml.append("</div>");
		}
		
		return updateHtmlContents("#mainButtonList", newHtml.toString());
	}

	@Override
	protected String updateButtonList_CombatMode()
	{
		StringBuilder newHtml = new StringBuilder();
		
		newHtml.append("<div class='titlebar'>COMBAT ACTIONS</div>");
		newHtml.append("<div class='backdrop2a navigationbox'>");
		newHtml.append("	<div class='titlebar'>Combat</div>");
		newHtml.append("	<a href='/main.jsp' class='button2' title='This will take you back to the old UI.'>Old UI</a>");
		newHtml.append("</div>");
		newHtml.append("</div>");
		
		return updateHtmlContents("#mainButtonList", newHtml.toString());
	}

	
	
	
	
}
