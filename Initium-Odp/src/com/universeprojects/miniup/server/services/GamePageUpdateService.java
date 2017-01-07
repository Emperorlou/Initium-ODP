package com.universeprojects.miniup.server.services;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.OperationBase;

public class GamePageUpdateService extends MainPageUpdateService {

	public GamePageUpdateService(ODPDBAccess db, CachedEntity user,
			CachedEntity character, CachedEntity location,
			OperationBase operation) {
		super(db, user, character, location, operation);
	}

	@Override
	protected String updateButtonList_NormalMode()
	{
		StringBuilder newHtml = new StringBuilder();
		
		newHtml.append("<div class='titlebar'>ACTIONS</div>");
		newHtml.append("<div class='backdrop2a navigationbox'>");
		newHtml.append("	<div class='titlebar'>This location</div>");
		newHtml.append("	<a href='#' class='button2' shortcut='69' onclick='doExplore(false)'><span class='shortcut-key'>(E)</span>Explore</a>");
		newHtml.append("<a class='button2' href='#' shortcut='87' onclick='doExplore(true)'><img src='https://initium-resources.appspot.com/images/ui/ignore-combat-sites.png' title='This button allows you to explore while ignoring combat sites. The shortcut key for this is W.' border=0/></a>");
		newHtml.append("	<div class='button2'>Aera</div>");
		newHtml.append("</div>");
		
					
//		newHtml.append("<br>");
//		
//		
//		if ("CampSite".equals(location.getProperty("type")))
//		{
//			newHtml.append("<a onclick='campsiteDefend()' class='main-button' shortcut='68' onclick='popupPermanentOverlay(\"Defending...\", \"You are looking out for threats to your camp.\")'><span class='shortcut-key'>(D)</span>Defend</a>");
//			newHtml.append("<br>");
//		}
//
//		if ("RestSite".equals(location.getProperty("type")) || "CampSite".equals(location.getProperty("type")))
//		{
//			newHtml.append("<a href='#' class='main-button' shortcut='82' onclick='doRest()'><span class='shortcut-key'>(R)</span>Rest</a>");
//			newHtml.append("<br>");
//		}
//
//		if (location.getProperty("name").toString().equals("Aera Inn"))
//		{
//			newHtml.append("<a class='main-button' onclick='doDrinkBeer(event)'>Drink Beer</a>");
//			newHtml.append("<br>");
//		}
//
//		if (getGroup()!=null && location.getProperty("ownerKey")!=null && ((Key)location.getProperty("ownerKey")).getId() == user.getKey().getId())
//		{
//			newHtml.append("<a href='#' class='main-button' onclick='giveHouseToGroup()'>Give this property to your group</a>");
//			newHtml.append("<br>");
//		}
//		
//		if (db.isCharacterAbleToCreateCampsite(db.getDB(), character, location))
//		{
//			newHtml.append("<a href='#' class='main-button' onclick='createCampsite()'>Create a campsite here</a>");
//			newHtml.append("<br>");
//		}
//
//		if ("CityHall".equals(location.getProperty("type")))
//		{
//			newHtml.append("<a href='#' class='main-button' onclick='buyHouse(event)'>Buy a new house</a>");
//			newHtml.append("<br>");
//		}
//		
//		if (user!=null && GameUtils.equals(location.getProperty("ownerKey"), user.getKey()))
//		{
//			newHtml.append("<a onclick='renamePlayerHouse(event)' class='main-button'>Rename this house</a>");
//		}
		
		newHtml.append("<div class='backdrop2a navigationbox'>");
		newHtml.append("	<div class='titlebar'>Paths</div>");
		newHtml.append("	<div class='button2'>Aera</div>");
		newHtml.append("</div>");
		newHtml.append("<div class='backdrop2a navigationbox'>");
		newHtml.append("	<div class='titlebar'>Properties</div>");
		newHtml.append("	<div class='button2'>Armory</div>");
		newHtml.append("</div>");
		newHtml.append("<div class='backdrop2a navigationbox'>");
		newHtml.append("	<div class='titlebar'>Combat Sites</div>");
		newHtml.append("	<div class='button2'>Troll</div>");
		newHtml.append("</div>");
		
		return updateHtmlContents("#mainButtonList", newHtml.toString());
	}

	@Override
	protected String updateButtonList_CombatMode()
	{
		// TODO Auto-generated method stub
		return super.updateButtonList_CombatMode();
	}

	
	
	
	
}
