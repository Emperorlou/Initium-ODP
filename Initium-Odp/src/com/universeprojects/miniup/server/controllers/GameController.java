package com.universeprojects.miniup.server.controllers;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

/**
 * Main Page controller for the new layout
 * @author tacobowl8
 */
@Controller
public class GameController extends PageController {
	public GameController() {
		super("view_profile");
	}

	@Override
	protected String processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		return "/test.jsp";
	}
}
