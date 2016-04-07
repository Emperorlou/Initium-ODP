package com.universeprojects.miniup.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;

public abstract class JspSnippets
{

	public JspSnippets()
	{
	}

	public static void allowPopupMessages(JspWriter out, HttpServletRequest request) throws IOException
	{
		allowPopupMessages(out, request, true);
	}
	
	public static void allowPopupMessages(JspWriter out, HttpServletRequest request, boolean cleanupParams) throws IOException
	{
		
		
		String message = null;
		String errorMessage = null;

		out.println("	<div id='popups'></div>");
//		if (request.getParameter("message")!=null)
//		{
//			message = request.getParameter("message");
//			message = message.replace("<br>", "\n");
//			message = message.replaceAll("<.*?>", "");
//			message = message.replaceAll("[<>'\"]", "");
//			message = message.replace("\n", "<br>");
//		}
//		if (request.getParameter("error")!=null)
//		{
//			errorMessage = request.getParameter("error");
//			errorMessage = errorMessage.replace("<br>", "\n");
//			errorMessage = errorMessage.replaceAll("<.*?>", "");
//			errorMessage = errorMessage.replaceAll("[<>'\"]", "");
//			errorMessage = errorMessage.replace("\n", "<br>");
//		}
		
		if (request.getAttribute("message")!=null)
			message = (String)request.getAttribute("message");

		if (request.getAttribute("error")!=null)
			errorMessage = (String)request.getAttribute("error");


		// Show messages
		if (message!=null)
		{
			if (message.length()>0)
			{
				out.println("<script type='text/javascript'>popupMessage('System Message', '"+
						message.replace("'", "\\'")+
						"');</script>");
			}
		}

		// Show error
		if (errorMessage!=null)
		{
			if (errorMessage.length()>0)
			{
				out.println("<script type='text/javascript'>popupMessage('An error occured', '"+
						errorMessage.replace("'", "\\'")+
						"');</script>");
			}
		}
		String queryString = request.getQueryString();
		StringBuffer url = request.getRequestURL();
		if(queryString != null && queryString.trim().isEmpty() == false) {
			queryString = queryString.replaceAll("message=[^&]*", "");
			queryString = queryString.replaceAll("error=[^&]*", "");
			queryString = queryString.replaceAll("&&+","&");
			if(queryString.startsWith("&"))
				queryString = queryString.substring(1);
			if(queryString.trim().isEmpty() == false)
				url.append("?").append(queryString);
		}


		//Cleanup parameters
		if (cleanupParams)
			out.println("<script type='text/javascript'>window.history.replaceState(null, document.title, \""+url.toString()+"\");</script>");
		
		
		
	}


	/**
	 * This specifically handles SecurityExceptions. See the handleGeneralErrorMessage() method for details.
	 * @param e
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public static void handleSecurityException(SecurityException e, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		if (e.getMessage()==null)
			handleGeneralErrorMessage("Please login before you can use the rest of the site.", "login.jsp?rtn="+WebUtils.encode(WebUtils.getFullURL(request)), request, response, false);
		else
			handleGeneralErrorMessage(e.getMessage(), "login.jsp?rtn="+WebUtils.encode(WebUtils.getFullURL(request)), request, response, true);
	}

	/**
	 * This specifically handles SecurityExceptions. See the handleGeneralErrorMessage() method for details.
	 * @param e
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public static void handleSecurityException_Ajax(SecurityException e, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		GameUtils.setPopupError(request, "You are not logged in. You have to <a href='login.jsp'>login here</a> before you'll be able to do anything else.");
		WebUtils.forceRedirectClientTo("ajaxresponse.jsp", request, response);
	}

	public static void handleGeneralErrorMessage(String message, String redirectToUrl, HttpServletRequest request, HttpServletResponse response, boolean error) throws ServletException, IOException
	{
		if (redirectToUrl==null)
		{
			handleGeneralErrorMessage(message, request, response);
			return;
		}
		String paramType = "message";
		if (error)
			paramType = "error";

		String url = redirectToUrl;
		if (url.contains("?"))
			url+="&"+paramType+"="+WebUtils.encode(message);
		else
			url+="?"+paramType+"="+WebUtils.encode(message);

		WebUtils.forceRedirectClientTo(url, request, response);
	}

	/**
	 * This redirects the user back to the page that initiated the request, and sends back an error message that is expected to be handled by the popups.
	 * 
	 * @param message
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public static void handleGeneralErrorMessage(String message, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String url = request.getHeader("referer").split("[?]")[0];
		handleGeneralErrorMessage(message, url, request, response, true);
	}



}
