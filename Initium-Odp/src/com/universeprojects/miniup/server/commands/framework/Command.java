package com.universeprojects.miniup.server.commands.framework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.OperationBase;
import com.universeprojects.miniup.server.UserRequestIncompleteException;

public abstract class Command extends OperationBase
{
	public enum JavascriptResponse
	{
		None,
		FullPageRefresh,
		ReloadPagePopup
	}
	protected final ODPDBAccess db;
	protected final CachedDatastoreService ds;
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	
	private String popupMessage;
	private JavascriptResponse jsResponse = JavascriptResponse.None;
	private Map<String, Object> callbackData = new HashMap<String, Object>();
	
	public Command(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		this.request = request;
		this.response = response;
		
		this.db = db;
		this.ds = db.getDB();
	}
	
	protected ODPDBAccess getDB()
	{
		return db;
	}
	
	protected CachedDatastoreService getDS()
	{
		return getDB().getDB();
	}
	
	protected void debugLog(String log)
	{
		db.sendGameMessage(db.getDB(), db.getCurrentCharacter(), "[DEBUG] "+log);
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
		} catch (Exception e) {
			throw new RuntimeException(this.getClass().getSimpleName()+" invalid call format, '"+fieldName+"' is not a valid id.", e);
		}
	}
	
	/** 
	 * Tries to fetch fieldName from param list and throws if it cannot.
	 * 
	 * @param params
	 * @param fieldName
	 * @param delimitingCharacter
	 * @return List of longs, parsed from param string
	 */
	protected List<Long> tryParseStringToLongList(Map<String,String> params, String fieldName, String delimitingCharacter)
	{
		try {
			String unparsedString = params.get(fieldName);
			String[] parsedArray = unparsedString.split(delimitingCharacter);
			ArrayList<Long> parsedList = new ArrayList<Long>();
			for(int i = 0; i < parsedArray.length; i++) {
				parsedList.add(Long.parseLong(parsedArray[i]));
			}
			return parsedList;
		} catch (Exception e) {
			throw new RuntimeException(this.getClass().getSimpleName()+" invalid call format, '"+fieldName+"' is not a valid delimited list.", e);
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
	public abstract void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException;

	
	/**
	 * Used to get all the callback data that was put into this command. 
	 * @return
	 */
	public Map<String,Object> getCallbackDataMap() 
	{
		return callbackData;
	}

	
	
}
