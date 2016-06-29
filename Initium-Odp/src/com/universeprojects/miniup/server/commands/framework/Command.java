package com.universeprojects.miniup.server.commands.framework;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.miniup.server.ODPDBAccess;

public abstract class Command 
{
	public enum JavascriptResponse
	{
		None,
		FullPageRefresh,
		ReloadPagePopup
	}
	private final ODPDBAccess db;
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	
	private String popupMessage;
	private JavascriptResponse jsResponse = JavascriptResponse.None;
	private Map<String, Object> callbackData = new HashMap<String, Object>();
	
	public Command(HttpServletRequest request, HttpServletResponse response)
	{
		this.request = request;
		this.response = response;
		
		this.db = new ODPDBAccess();
	}
	
	protected ODPDBAccess getDB()
	{
		return db;
	}
	
	protected CachedDatastoreService getDS()
	{
		return getDB().getDB();
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
	 * Tries to fetch fieldName from param list and throws if it cannot.
	 * 
	 * @param params
	 * @param fieldName
	 * @return Long id, parsed from param string
	 */
	protected Long tryParseId(Map<String,String> params, String fieldName)
	{
		try {
			return Long.parseLong(params.get(fieldName));
		} catch (Exception _) {
			throw new RuntimeException(this.getClass().getSimpleName()+" invalid call format, '"+fieldName+"' is not a valid id.");
		}
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
	 * Use this to pass data back to the calling javascript of this command.
	 * 
	 * @param field
	 * @param value
	 */
	protected void addCallbackData(String field, Object value)
	{
		if (field.matches("[A-Za-z0-9_]+")==false)
			throw new RuntimeException("Callback fields must be valid javascript field identifiers. They can only contain letters, numbers, and underscores.");
		
		callbackData.put(field,  value);
	}
	
	/**
	 * Use this to get data you previously added using .addCallbackData().
	 * 
	 * @param field
	 * @return
	 */
	protected Object getCallbackData(String field)
	{
		if (field.matches("[A-Za-z0-9_]+")==false)
			throw new RuntimeException("Callback fields must be valid javascript field identifiers. They can only contain letters, numbers, and underscores.");
		
		return callbackData.get(field);
	}
	
	/**
	 * The command's execution logic is done here. 
	 */
	public abstract void run(Map<String, String> parameters) throws UserErrorMessage;

	
	/**
	 * Used to get all the callback data that was put into this command. 
	 * @return
	 */
	public Map<String,Object> getCallbackDataMap() 
	{
		return callbackData;
	}
	
	
	
}
