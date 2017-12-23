package com.universeprojects.miniup.server.controllers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

@Controller
public class SublocationsController extends PageController {
	
	public SublocationsController() {
		super("sublocations");
	}
	
	@Override
	protected final String processRequest(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
		
	    
		
	    return "/WEB-INF/odppages/ajax_sublocations.jsp";
	}
	
	
}
