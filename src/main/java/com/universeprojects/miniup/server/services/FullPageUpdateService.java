package com.universeprojects.miniup.server.services;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.OperationBase;

public class FullPageUpdateService extends ExperimentalPageUpdateService { 

	public FullPageUpdateService(ODPDBAccess db, CachedEntity user,
			CachedEntity character, CachedEntity location,
			OperationBase operation) {
		super(db, user, character, location, operation);
	}
	
	@Override
	protected String getSideBannerLinks()
	{
		StringBuilder newHtml = new StringBuilder();

		newHtml.append("<div class='path-overlay-link major-banner-links' style='left:50%; top:15%;'>");
		newHtml.append("<a id='thisLocation-button' class='button-overlay-major' onclick='makeIntoPopup(\".this-location-box\")' style='right:0px;top:0px;'><img alt='Location actions' src='/images/ui/magnifying-glass2.png'></a>");			
		newHtml.append("<a id='guard-button' class='button-overlay-major' onclick='viewGuardSettings()' style='right:4px;top:142px;'><img alt='Guard settings' src='/images/ui/guardsettings1.png'></a>");
		newHtml.append("</div>");
		
		return newHtml.toString();
	}
	
	@Override
	protected String updateButtonList_NormalMode()
	{
		StringBuilder newHtml = new StringBuilder();

//		newHtml.append("<div class='main-buttonbox' style='display:block'>");
//		newHtml.append("</div>");
		
		newHtml.append("<div class='main-buttonbox v3-window3 this-location-box'>");
		newHtml.append("<h4>This Location</h4>");
		
		newHtml.append("<div id='in-button-list-location-description'>");
		String desc = (String)location.getProperty("description");
		if (desc==null) desc = "";
		newHtml.append(desc);
		newHtml.append("</div>");
		
		
		newHtml.append("<a id='main-explore' href='#' class='v3-main-button' shortcut='69' onclick='doExplore(event, false)'><span class='shortcut-key'>(E)</span>Explore "+location.getProperty("name")+"</a>");
		newHtml.append("<br>");
		newHtml.append("<a id='main-explore-ignorecombatsites' class='v3-main-button' href='#' shortcut='87' onclick='doExplore(event, true)'><span class='shortcut-key'>(W)</span>Explore (no old sites)</a>");
		
		newHtml.append("<br>");
		newHtml.append("<div id='main-merchantlist' class='v3-main-button-half' onclick='loadLocationMerchants()' shortcut='83'>");
		newHtml.append("<span class='shortcut-key'> (S)</span>Nearby stores");
		newHtml.append("</div>");

		newHtml.append("<div id='main-itemlist' class='v3-main-button-half' onclick='loadLocationItems()' shortcut='86'>");
		newHtml.append("<span class='shortcut-key'> (V)</span>Nearby items");
		newHtml.append("</div>");
		
		newHtml.append("<br>");
		newHtml.append("<div id='main-characterlist' class='v3-main-button-half' onclick='loadLocationCharacters()' shortcut='66'>");
		newHtml.append("<span class='shortcut-key'> (B)</span>Nearby characters");
		newHtml.append("</div>");
		
		newHtml.append("<div class='v3-main-button-half'>");
		newHtml.append("<span class='shortcut-key'> </span>Nearby territory");
		newHtml.append("</div>");
					
		newHtml.append("<br>");
		
		
		if ("CampSite".equals(location.getProperty("type")))
		{
			newHtml.append("<a href='#' onclick='doCampDefend()' class='v3-main-button' shortcut='68'><span class='shortcut-key'>(D)</span>Defend</a>");
			newHtml.append("<br>");
		}

		if ("RestSite".equals(location.getProperty("type")) || "CampSite".equals(location.getProperty("type")))
		{
			newHtml.append("<a href='#' class='v3-main-button' shortcut='82' onclick='doRest()'><span class='shortcut-key'>(R)</span>Rest</a>");
			newHtml.append("<br>");
		}

		if (location.getProperty("name").toString().equals("Aera Inn"))
		{
			newHtml.append("<a class='v3-main-button' onclick='doDrinkBeer(event)'>Drink Beer</a>");
			newHtml.append("<br>");
		}

		if (getGroup()!=null && location.getProperty("ownerKey")!=null && ((Key)location.getProperty("ownerKey")).getId() == user.getKey().getId())
		{
			newHtml.append("<a href='#' class='v3-main-button' onclick='giveHouseToGroup()'>Give this property to your group</a>");
			newHtml.append("<br>");
		}
		
		if (db.isCharacterAbleToCreateCampsite(db.getDB(), character, location))
		{
			if("RestSite".equals(location.getProperty("type")))
				newHtml.append("<a href='#' class='v3-main-button' onclick='createCampsite()'>Create a campsite here</a>");
			else
				newHtml.append("<a href='#' class='v3-main-button' shortcut='82' onclick='createCampsite()'><span class='shortcut-key'>(R)</span>Create a campsite here</a>");
			newHtml.append("<br>");
		}

		if ("CityHall".equals(location.getProperty("type")))
		{
			newHtml.append("<a href='#' class='v3-main-button' onclick='buyHouse(event)'>Buy a new house</a>");
			newHtml.append("<br>");
		}
		
		if (user!=null && GameUtils.equals(location.getProperty("ownerKey"), user.getKey()))
		{
			newHtml.append("<a onclick='createMapToHouse(event)' class='v3-main-button'>Create map to this house</a><br/>");
			newHtml.append("<a onclick='renamePlayerHouse(event)' class='v3-main-button'>Rename this house</a>");
		}

		newHtml.append("</div>");

		return updateHtmlContents("#main-button-list", newHtml.toString());
	}	
	
}
