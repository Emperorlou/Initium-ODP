package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.InitiumPageController;
import com.universeprojects.miniup.server.NotLoggedInException;
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
		ODPDBAccess db = ODPDBAccess.getInstance(request);
		try{InitiumPageController.requireLoggedIn(db);}catch(NotLoggedInException e){return InitiumPageController.loginMessagePage;}
		
		CachedEntity character = db.getCurrentCharacter();
		
		
		List<String> equipList = new ArrayList<String>();
		for(String slot:ODPDBAccess.EQUIPMENT_SLOTS)
		{
			StringBuilder sb = new StringBuilder();
			CachedEntity item = db.getEntity((Key)character.getProperty("equipment"+slot));
			
			if (item==null && slot.equals("Pet")) continue;
			
			sb.append("<div class='main-item equip-item'><span class='equip-slot'>"+slot+": </span>");
			// Allow updating specific slots via commands.
			// Selector would be ".equip-item span[rel='RightHand']"
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
