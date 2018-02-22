package com.universeprojects.miniup.server.controllers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.NotLoggedInException;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.WebUtils;
import com.universeprojects.miniup.server.services.MainPageUpdateService;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

@Controller
public class ExperimentalPageController extends PageController {

	public ExperimentalPageController() {
		super("experimental");
	}

	@Override
	protected String processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		ODPDBAccess db = ODPDBAccess.getInstance(request);
		
		
		
		if (db.isLoggedIn(getRequest())==false)
		{
			WebUtils.askForRedirectClientTo("/landing.jsp", request, response);
			return null;
		}

		try
		{
			request.setAttribute("verifyCode", db.getVerifyCode());
		}
		catch (NotLoggedInException e)
		{
			// Shouldn't happen
			throw new RuntimeException(e);
		}

		
		response.addHeader("Access-Control-Allow-Origin", "*");

		long serverTime = System.currentTimeMillis();
		request.setAttribute("serverTime", serverTime);
		
		request.setAttribute("isTestServer", GameUtils.isTestServer(request));
		
		request.setAttribute("userId", db.getCurrentUserKey().getId());		
		
		// Getting user data...
		CachedEntity user = db.getCurrentUser();
		Boolean isPremium = false;
		if (user!=null)
			isPremium = (Boolean)user.getProperty("premium");
		if (isPremium==null) isPremium = false;
		request.setAttribute("isPremium", isPremium);

		
		
		// Getting character data...
		CachedEntity character = db.getCurrentCharacter();
		if (character==null)
			throw new RuntimeException("Character is null. We have to code something to handle this.");
		
		request.setAttribute("chatIdToken", db.getChatIdToken(character.getKey()));
		if (user!=null)
			request.setAttribute("newChatIdToken", db.getNewChatAuthToken(user.getKey()));
		request.setAttribute("characterId", character.getId());
		
		
		// Getting location data...
		CachedEntity location = db.getEntity((Key)character.getProperty("locationKey"));
		boolean isOutside = false;
		if ("TRUE".equals(location.getProperty("isOutside")))
			isOutside = true;
		request.setAttribute("isOutside", isOutside);

		String biome = (String)location.getProperty("biomeType");
		if (biome==null) biome = "Temperate";
		request.setAttribute("biome", biome);
		String locationAudioDescriptor = (String)location.getProperty("audioDescriptor");
		if (locationAudioDescriptor==null) locationAudioDescriptor = "";
		request.setAttribute("locationAudioDescriptor", locationAudioDescriptor);
		
		String locationAudioDescriptorPreset = (String)location.getProperty("audioDescriptorPreset");
		if (locationAudioDescriptorPreset==null) locationAudioDescriptorPreset = "";
		request.setAttribute("locationAudioDescriptorPreset", locationAudioDescriptorPreset);
		
		
		
		

		MainPageUpdateService updateService = new MainPageUpdateService(db, db.getCurrentUser(), character, location, null);
		
		request.setAttribute("locationName", updateService.updateLocationName());
		request.setAttribute("mainMoneyIndicator", updateService.updateMoney());
		request.setAttribute("bannerTextOverlay", updateService.updateInBannerOverlayLinks());
		request.setAttribute("mainButtonList", updateService.updateButtonList());
		request.setAttribute("bannerJs", updateService.updateLocationJs());	
		request.setAttribute("activePlayers", updateService.updateActivePlayerCount());
		request.setAttribute("buttonBar", updateService.updateButtonBar());
		request.setAttribute("locationDescription", updateService.updateLocationDescription());
		request.setAttribute("territoryViewHtml", updateService.updateTerritoryView());
		request.setAttribute("partyPanel", updateService.updatePartyView());
		request.setAttribute("collectablesPanel", updateService.updateCollectablesView());
		request.setAttribute("locationScripts", updateService.updateLocationDirectScripts());
		request.setAttribute("inBannerCharacterWidget", updateService.updateInBannerCharacterWidget());
		request.setAttribute("inBannerCombatantWidget", updateService.updateInBannerCombatantWidget());
		request.setAttribute("testPanel", updateService.updateTestPanel());
		request.setAttribute("immovablesPanel", updateService.updateImmovablesPanel()); 
		request.setAttribute("midMessagePanel", updateService.updateMidMessagePanel());
		request.setAttribute("locationQuicklist", updateService.updateLocationQuicklist());
		request.setAttribute("monsterCountPanel", updateService.updateMonsterCountPanel());
		request.setAttribute("globalNavigationMap", updateService.updateGlobalNavigationMap());
		
		
		if (db.getCurrentCharacter().isUnsaved())
			db.getDB().put(db.getCurrentCharacter());

		
		return "/WEB-INF/odppages/experimental.jsp";
	}
	
	
	
}
