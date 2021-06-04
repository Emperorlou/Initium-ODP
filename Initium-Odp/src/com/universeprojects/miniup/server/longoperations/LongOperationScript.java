package com.universeprojects.miniup.server.longoperations;

import java.util.Map;

import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.scripting.actions.ScriptActionCallback;
import com.universeprojects.miniup.server.scripting.actions.ScriptActionSimple;
import com.universeprojects.miniup.server.scripting.events.ScriptEvent;

public class LongOperationScript extends LongOperation{
	
	public LongOperationScript(ODPDBAccess db, Map<String, String[]> requestParameters) throws UserErrorMessage {
		super(db, requestParameters);
		// TODO Auto-generated constructor stub
	}

	@Override
	int doBegin(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException {
		
		setDataProperty("paramMap", parameters); //TODO make sure we can serialize a string-string map
		
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
		
		@SuppressWarnings("unchecked")
		ScriptActionCallback action = new ScriptActionCallback(db, this, (Map<String, String>) getDataProperty("paramMap"));
		ScriptEvent event = action.execute();
		
		return (String) event.getAttribute("message");
	}

	@Override
	public String getPageRefreshJavascriptCall() {
		return "doLongTriggerEffect(null)"; //TODO did I do this properly?
	}

}
