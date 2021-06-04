package com.universeprojects.miniup.server.longoperations;

import java.util.Map;

import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.scripting.actions.ScriptActionSimple;
import com.universeprojects.miniup.server.scripting.events.ScriptEvent;

public class LongOperationScript extends LongOperation{

	public LongOperationScript(ODPDBAccess db, Map<String, String[]> requestParameters) throws UserErrorMessage {
		super(db, requestParameters);
		// TODO Auto-generated constructor stub
	}

	@Override
	int doBegin(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException {
		
		ScriptActionSimple action = new ScriptActionSimple(db, this, parameters);
		ScriptEvent event = action.execute();
		
		setLongOperationName((String) event.getAttribute("actionName"));
		setLongOperationDescription((String) event.getAttribute("actionDescription"));
		
		putToCallback("title", (String) event.getAttribute("overlayTitle"));
		putToCallback("text", (String) event.getAttribute("overlayText"));
		putToCallback("image", (String) event.getAttribute("overlayImage"));
		
		
		return (Integer) event.getAttribute("time");
	}

	@Override
	String doComplete() throws UserErrorMessage, UserRequestIncompleteException, ContinuationException {
		
		//run a second set of custom logic
		
		//return a custom message
		return null;
	}

	@Override
	public String getPageRefreshJavascriptCall() {
		// TODO Auto-generated method stub
		return null;
	}

}
