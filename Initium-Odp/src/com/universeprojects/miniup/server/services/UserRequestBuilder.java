package com.universeprojects.miniup.server.services;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParserFactory;
import org.json.simple.parser.ParseException;

import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.OperationBase;
import com.universeprojects.miniup.server.UserRequestIncompleteException;

public abstract class UserRequestBuilder<T> extends Service
{
	final protected OperationBase command;
	final protected String uniqueId;
	
	protected String playerMessage = null;
	
	public UserRequestBuilder(String uniqueId, ODPDBAccess db, OperationBase command)
	{
		super(db);
		this.command = command;
		this.uniqueId = uniqueId;
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
	protected abstract String getJavascriptRecallFunction();
	
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

		throw new UserRequestIncompleteException(getPagePopupTitle(), getPagePopupUrl(), playerMessage, uniqueId);
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
