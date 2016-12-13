package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.GroupService;

public class CommandGroupMergeAcceptApplication extends Command {

	public CommandGroupMergeAcceptApplication(ODPDBAccess db, HttpServletRequest request,
			HttpServletResponse response) {
		super(db, request, response);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();

		Long groupID = parameters.containsKey("groupId") ? tryParseId(parameters, "groupId") : null;
		if(groupID == null) throw new RuntimeException("Command missing parameter groupId");
		
		CachedEntity group = db.getEntity("Group", groupID);
		GroupService service = new GroupService(db, character);
		try
		{
			if(service.acceptMergeApplicationFrom(ds, group))
				setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
			else
				throw new UserErrorMessage("Unexpected issue merging groups!");
		}
		catch(Exception ex)
		{
			if(service.characterHasGroup() == false)
				throw new UserErrorMessage("Character does not belong to a group!");
			if(service.isCharacterGroupAdmin() == false)
				throw new UserErrorMessage("Character is not an admin of the group!");
			if(service.isCharacterInSpecifiedGroup(group))
				throw new RuntimeException("Unable to merge identical groups!");
			throw ex;
		}
	}

}
