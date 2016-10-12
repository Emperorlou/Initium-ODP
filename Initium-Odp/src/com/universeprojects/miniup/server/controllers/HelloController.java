package com.universeprojects.miniup.server.controllers;

import com.universeprojects.web.Controller;
import com.universeprojects.web.PageController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Controller
public class HelloController extends PageController {
	
	public HelloController() {
		super("hello");
	}

@Override
protected final String processRequest(HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException {
	
	// set request attributes, to be accessible in the JSP page
	String msg = "Hello World!";
	request.setAttribute("message", msg);
	
	return "/WEB-INF/odppages/hello.jsp";
	}
}