package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.services.ItemFilterService;
import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

@Controller
public class ItemFilterController extends PageController {
	
	public ItemFilterController() {
		super("itemfilters"); 
	}

	@Override
	protected String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		ODPDBAccess db = ODPDBAccess.getInstance(request);
		
		Map<String, String> filters = new ItemFilterService(db).getFilters();
		
		List<String> data = new ArrayList<>();
		
		for(Entry<String,String> entry : filters.entrySet()) 
			data.add("<a class=" + entry.getValue() + ">" + entry.getKey() + "</a><br>");
		
		request.setAttribute("itemFilters", data);
		request.setAttribute("hasFilters", data.size() > 0);
		
		return "/WEB-INF/odppages/itemfilters.jsp";
	}
}