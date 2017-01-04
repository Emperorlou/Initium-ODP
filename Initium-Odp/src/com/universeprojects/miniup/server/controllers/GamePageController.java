package com.universeprojects.miniup.server.controllers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.services.CombatService;
import com.universeprojects.miniup.server.services.GamePageUpdateService;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

@Controller
public class GamePageController extends PageController {

	public GamePageController() {
		super("game");
	}

	@Override
	protected String processRequest(HttpServletRequest request,
			HttpServletResponse arg1) throws ServletException, IOException {
		
		ODPDBAccess db = new ODPDBAccess(request);
		CachedEntity character = db.getCurrentCharacter();
		CachedEntity location = db.getEntity((Key)character.getProperty("locationKey"));
		CombatService combatService = new CombatService(db);

		GamePageUpdateService updateService = new GamePageUpdateService(db, db.getCurrentUser(), character, location, null);
		
		request.setAttribute("bannerTextOverlay", updateService.updateInBannerOverlayLinks());
		request.setAttribute("inBannerCharacterWidget", updateService.updateInBannerCharacterWidget());
		request.setAttribute("locationId", location.getKey().getId());
		request.setAttribute("mainGoldIndicator", updateService.updateMoney());
		request.setAttribute("mainButtonList", updateService.updateButtonList(combatService));
		request.setAttribute("bannerJs", updateService.updateLocationJs());	
		request.setAttribute("activePlayers", updateService.updateActivePlayerCount());
		request.setAttribute("buttonBar", updateService.updateButtonBar());
		request.setAttribute("locationDescription", updateService.updateLocationDescription());
		request.setAttribute("territoryViewHtml", updateService.updateTerritoryView());
		request.setAttribute("partyPanel", updateService.updatePartyView());
		request.setAttribute("locationScripts", updateService.updateLocationDirectScripts());
		request.setAttribute("locationName", updateService.updateLocationName());
		request.setAttribute("locationDescription", updateService.updateLocationDescription());

		return "/WEB-INF/odppages/game.jsp";
	}
}
