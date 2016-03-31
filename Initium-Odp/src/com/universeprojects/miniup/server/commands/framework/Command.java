package com.universeprojects.miniup.server.commands.framework;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.ODPDBAccess;

public abstract class Command 
{
	enum JavascriptResponse
	{
		None,
		FullPageRefresh,
		ReloadPagePopup
	}
	private ODPDBAccess db;
	private CachedDatastoreService ds;
	private HttpServletRequest request;
	private HttpServletResponse response;
	
	private String popupMessage;
	private JavascriptResponse jsResponse = JavascriptResponse.None;
	
	public Command(HttpServletRequest request, HttpServletResponse response)
	{
		this.ds = ds;
		this.request = request;
		this.response = response;
		
		this.db = new ODPDBAccess();
	}
	
	protected ODPDBAccess getDB()
	{
		return db;
	}
	
	/**
	 * When the command is complete, the given message will appear in a popup on the player's screen.
	 * 
	 * @param message
	 */
	protected void setPopupMessage(String message)
	{
		this.popupMessage = message;
	}
	
	
	protected String getPopupMessage()
	{
		return popupMessage;
	}
	
	/**
	 * When the command finishes, the javascript on the client side will have an opportunity 
	 * to do something. There are a sent number of choices for this as represented in the
	 * JavascriptResponse enum. The default is None.
	 * 
	 * @param jsResponse
	 */
	protected void setJavascriptResponse(JavascriptResponse jsResponse)
	{
		this.jsResponse = jsResponse;
	}
	
	protected JavascriptResponse getJavascriptResponse()
	{
		return jsResponse;
	}
	
	
	/**
	 * The command's execution logic is done here. 
	 */
	public abstract void run(Map<String, String> parameters) throws UserErrorMessage;
	
	
	
}
