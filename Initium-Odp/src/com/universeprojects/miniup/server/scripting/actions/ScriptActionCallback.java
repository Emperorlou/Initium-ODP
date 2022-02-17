package com.universeprojects.miniup.server.scripting.actions;

import java.util.Map;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.OperationBase;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.scripting.events.ScriptEvent;

public class ScriptActionCallback extends ScriptAction{

	public ScriptActionCallback(ODPDBAccess db, OperationBase operation, Map<String, String> parameters) {
		super(db, operation, parameters);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void validateCharacterState(CachedEntity character) throws UserErrorMessage {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void validateScriptState(CachedEntity trigger, CachedEntity script) throws UserErrorMessage {
		
		//Given that this is a callback, replace the script with its callback script.
		//TODO consider embedding this
		Key ref = (Key) script.getProperty("callback");
		script = db.getEntity(ref);
		
	}

	@Override
	protected ScriptEvent generateEvent(CachedEntity character, CachedEntity trigger, CachedEntity script,
			Map<String, String> parameters) throws UserErrorMessage {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void processParameters(ScriptEvent event, Map<String, String> parameters) throws UserErrorMessage {
		// TODO Auto-generated method stub
		
	}

}
