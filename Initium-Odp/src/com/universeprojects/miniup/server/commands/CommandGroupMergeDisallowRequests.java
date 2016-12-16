package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.GroupService;

public class CommandGroupMergeDisallowRequests extends Command {

	public CommandGroupMergeDisallowRequests(ODPDBAccess db,
			HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {

		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();

		GroupService service = new GroupService(db, character);
		CachedEntity charGroup = service.getCharacterGroup();
		
		// Checks for null of the passed in group, so safe to call.
		if(service.setDisallowMergeRequests(charGroup))
		{
			// property was updated by the service. Save group and update the HTML. 
			ds.put(charGroup);
			updateHtml("#mergeRequestDisallow", "<a id='mergeRequestAllow' onclick='groupMergeRequestsAllow(event)'>Allow Merge Requests</a>");
		}
		else
		{
			if(service.characterHasGroup()==false)
				throw new UserErrorMessage("Character does not belong to a group!");
			if(service.isCharacterGroupAdmin() == false)
				throw new UserErrorMessage("Character is not an admin of this group!");
			
			throw new UserErrorMessage("Unexpected error disallowing merge requests! Please contact a dev.");
		}
	}

}
