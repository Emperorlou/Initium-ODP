package com.universeprojects.miniup.server.services;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.universeprojects.json.shared.JSONObject;
import com.universeprojects.json.shared.parser.JSONParserFactory;
import com.universeprojects.json.shared.parser.ParseException;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.OperationBase;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.WebUtils;

public abstract class UserRequestBuilder<T> extends Service
{
	final protected OperationBase command;
	final protected String uniqueId;
	protected String jsInitiatingFunctionCall;
	
	protected String playerMessage = null;
	
	
	public UserRequestBuilder(String uniqueId, ODPDBAccess db, OperationBase command, String jsInitiatingFunctionCall)
	{
		super(db);
		this.command = command;
		this.uniqueId = uniqueId;
		this.jsInitiatingFunctionCall = jsInitiatingFunctionCall;
	}
	
	protected abstract T convertParametersToResult(JSONObject parameters);
	
	protected abstract String getPagePopupUrl();
	
	protected abstract String getPagePopupTitle();
	
	/**
	 * The implementing class needs to provide working javascript to call the function that
	 * initiated the command/long-operation in the first place, again. 
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private String getUrlParameters()
	{
		StringBuilder sb = new StringBuilder();
		HttpServletRequest request = db.getRequest();
		boolean firstTime = true;
		for(String key:((Map<String,String[]>)request.getParameterMap()).keySet())
		{
			if (firstTime) 
				firstTime = false;
			else
				sb.append("&");
			sb.append(key).append("=");
			sb.append(WebUtils.encode(request.getParameter(key)));
		}
		
		return sb.toString();
	}
	
	/**
	 * This message will appear as a popup for the user as soon as the user request builder's page popup has appeared.
	 * It will appear over top of the page popup.
	 * @param playerMessage
	 */
	public void setPlayerMessage(String playerMessage)
	{
		this.playerMessage = playerMessage;
	}
	
	
	public T go() throws UserRequestIncompleteException
	{
		if (isCompleted())
			return convertParametersToResult(getUserResponse());

		throw new UserRequestIncompleteException(getPagePopupTitle(), getPagePopupUrl(), WebUtils.textToHtml(getUrlParameters()), playerMessage, uniqueId);
	}

	private JSONObject getUserResponse()
	{
		HttpServletRequest request = db.getRequest();
		
		String uniqueParameterName = getUniqueParameterName();

		String data = request.getParameter(getUniqueParameterName());
		
		JSONObject dataObject;
		try
		{
			dataObject = (JSONObject)JSONParserFactory.getServerParser().parse(data);
		}
		catch (ParseException e)
		{
			throw new RuntimeException("Invalid user response.");
		}
				
		return dataObject;
	}
	
	private String getUniqueParameterName()
	{
		return "__"+uniqueId+"UserResponse";
	}
	
	public boolean isCompleted()
	{
		HttpServletRequest request = db.getRequest();
		
		String value = request.getParameter(getUniqueParameterName());

		if (value!=null && value.length()>2)
			return true;
		
		return false;
	}
}
