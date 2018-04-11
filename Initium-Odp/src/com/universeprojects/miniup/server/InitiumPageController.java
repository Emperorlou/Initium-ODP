package com.universeprojects.miniup.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.web.PageController;

public abstract class InitiumPageController extends PageController
{

	public InitiumPageController(String pageName)
	{
		super(pageName);
	}

	@Override
	protected String processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try
		{
			return processInitiumRequest(request, response);
		}
		catch(NotLoggedInException e)
		{
			return "/WEB-INF/odppages/notloggedin.jsp";
		}
	}

	protected abstract String processInitiumRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
	
	
	public static void requireLoggedIn(ODPDBAccess db) throws NotLoggedInException
	{
		db.getCurrentUserKey();
	}
	
	public static String loginMessagePage = "/WEB-INF/odppages/notloggedin.jsp";
}
