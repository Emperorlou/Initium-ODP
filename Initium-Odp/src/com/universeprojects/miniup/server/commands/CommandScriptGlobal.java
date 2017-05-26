package com.universeprojects.miniup.server.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.management.RuntimeErrorException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.scripting.events.GlobalEvent;
import com.universeprojects.miniup.server.scripting.events.ScriptEvent;
import com.universeprojects.miniup.server.scripting.wrappers.EntityWrapper;
import com.universeprojects.miniup.server.services.ContainerService;
import com.universeprojects.miniup.server.services.ScriptService;

public class CommandScriptGlobal extends CommandScriptBase {

	public CommandScriptGlobal(ODPDBAccess db, HttpServletRequest request,
			HttpServletResponse response) {
		super(db, request, response);
	}

	@Override
	protected void validateCharacterState(CachedEntity character)
			throws UserErrorMessage {
		// Script itself will fire the validation.
	}

	@Override
	protected void validateScriptState(CachedEntity trigger, CachedEntity script)
			throws UserErrorMessage {
		
	}

	@Override
	protected ScriptEvent generateEvent(CachedEntity character,
			CachedEntity trigger, CachedEntity script, Map<String, String> parameters) throws UserErrorMessage {
		String additionalEntities = parameters.get("entities");
		List<Key> entityFetch = new ArrayList<Key>();
		
		for(String str:additionalEntities.split(";"))
		{
			try
			{
				String[] tokens = str.split(":");
				entityFetch.add(KeyFactory.createKey(tokens[0], Long.parseLong(tokens[1])));
			}
			catch(Exception ex)
			{
				throw new RuntimeException("Invalid use of global script!", ex);
			}
		}
		
		List<CachedEntity> entities = db.getEntities(entityFetch);
		List<EntityWrapper> wrappers = new ArrayList<EntityWrapper>();
		for(CachedEntity ent:entities)
		{
			try
			{
				wrappers.add(ScriptService.wrapEntity(ent, db));
			}
			catch(Exception ex)
			{
				throw new UserErrorMessage("Something went wrong... Try again later.");
			}
		}
		
		GlobalEvent ge = new GlobalEvent(character, db);
		return new GlobalEvent(db, wrappers);
	}

}
