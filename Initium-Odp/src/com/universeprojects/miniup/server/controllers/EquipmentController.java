package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

@Controller
public class EquipmentController extends PageController {

	public EquipmentController() {
		super("equipmentlist");
	}

	@Override
	protected String processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setHeader("Access-Control-Allow-Origin", "*");     // This is absolutely necessary for phonegap to work
		
		ODPDBAccess db = ODPDBAccess.getInstance(request);
		CachedEntity character = db.getCurrentCharacter();
		
		List<String> equipList = new ArrayList<String>();
		for(String slot:ODPDBAccess.EQUIPMENT_SLOTS)
		{
			StringBuilder sb = new StringBuilder();
			sb.append("<div class='main-item equip-item'><span class='equip-slot'>"+slot+": </span>");
			CachedEntity item = db.getEntity((Key)character.getProperty("equipment"+slot));
			// Allow updating specific slots via commands.
			// Selector would be ".equip-item div[rel='RightHand']"
			sb.append("<span rel='" + slot + "'>");
			sb.append(GameUtils.renderEquipSlot(item));
			sb.append("</span>");
			sb.append("</div>");
			equipList.add(sb.toString());
		}
		request.setAttribute("equipList", equipList);
		return "/WEB-INF/odppages/equipmentlist.jsp";
	}

}
