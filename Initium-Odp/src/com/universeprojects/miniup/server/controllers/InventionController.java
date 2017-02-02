package com.universeprojects.miniup.server.controllers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.services.ODPInventionService;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

@Controller
public class InventionController extends PageController {
	
	public InventionController() {
		super("invention");
	}

	@Override
	protected final String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		ODPDBAccess db = ODPDBAccess.getInstance(request);
		ODPInventionService invention = db.getInventionService();
	
		return "/WEB-INF/odppages/ajax_invention.jsp";
	}
}