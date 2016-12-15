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

public class CommandGroupMergeCancelRequest extends Command {

	public CommandGroupMergeCancelRequest(ODPDBAccess db,
			HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		// TODO Auto-generated method stub
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();

		if(character.getProperty("groupKey") == null)
			throw new UserErrorMessage("Character does not belong to a group!");
		
		// Group refers to the group we will be merging with. GroupService handles security 
		// permissions, determining whether the character is an admin of his group.
		GroupService service = new GroupService(db, character);
		CachedEntity charGroup = service.getCharacterGroup();
		if(!service.cancelMergeRequest())
		{
			if(service.characterHasGroup() == false)
				throw new UserErrorMessage("You do not currently belong to a group");
			if(service.isCharacterGroupAdmin() == false)
				throw new UserErrorMessage("You are not a group admin");
			if(service.getMergeRequestGroupKeyFor(charGroup) == null)
				throw new UserErrorMessage("Your group does not have a pending merge request with any group");
			throw new UserErrorMessage("Unexpected error cancelling merge requests! Please contact a dev.");
		}
		
		// property was updated by the service. Save group and update the HTML. 
		ds.put(charGroup);
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
	}

}
