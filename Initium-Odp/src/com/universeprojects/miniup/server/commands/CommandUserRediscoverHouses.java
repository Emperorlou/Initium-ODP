package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.PropertiesService;

/**
 * Rediscovers all houses for the currently logged in user. Does not matter if a path
 * was forgotten, will rediscover anyway.
 * 
 * @author spfiredrake
 */
public class CommandUserRediscoverHouses extends Command {

	public CommandUserRediscoverHouses(ODPDBAccess db,
			HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		ODPDBAccess db = getDB();

		CachedEntity user = db.getCurrentUser();
		if(user==null)
			throw new UserErrorMessage("You cannot rediscover houses on throwaway accounts");
		new PropertiesService(db).rediscoverHouses(user);
		setJavascriptResponse(JavascriptResponse.FullPageRefresh);
	}
}
