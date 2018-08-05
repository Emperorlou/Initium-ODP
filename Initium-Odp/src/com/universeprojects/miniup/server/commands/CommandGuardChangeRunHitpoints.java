package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class CommandGuardChangeRunHitpoints extends Command
{

	public CommandGuardChangeRunHitpoints(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException
	{
		String rawValue = parameters.get("value");
		
		if (rawValue!=null && rawValue.equals("null") || rawValue.trim().equals(""))
			rawValue = null;
		
		Double value = null;
		if (rawValue!=null)
			value = Double.parseDouble(rawValue);
		
		Double maxHitpoints = (Double)db.getCurrentCharacter().getProperty("maxHitpoints");
		if (value!=null && value>maxHitpoints)
			value = maxHitpoints-1;
		
		if (value!=null && value<=0)
			value = null;
		
		db.getCurrentCharacter().setProperty("guardRunHitpoints", value);
		
		ds.put(db.getCurrentCharacter());
		
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
	}

}
