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
import com.universeprojects.miniup.server.commands.framework.Command.JavascriptResponse;
import com.universeprojects.miniup.server.services.GroupService;

public class CommandGroupMergeSubmitRequest extends Command {

	public CommandGroupMergeSubmitRequest(ODPDBAccess db, HttpServletRequest request,
			HttpServletResponse response) {
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
		
		Long groupID = parameters.containsKey("groupId") ? tryParseId(parameters, "groupId") : null;
		if(groupID == null) throw new RuntimeException("Command missing parameter groupId");
		
		CachedEntity group = db.getEntity("Group", groupID);
		
		// Group refers to the group we will be merging with. GroupService handles security 
		// permissions, determining whether the character is an admin of his group.
		GroupService service = new GroupService(db, character);
		
		
		CachedEntity charGroup = service.setRequestMergeWith(group);
		if(charGroup == null)
		{
			if(service.characterHasGroup() == false)
				throw new UserErrorMessage("Character does not belong to a group");
			if(service.isCharacterInSpecifiedGroup(group))
				throw new RuntimeException("Specified group is characters own group");
			if(service.isCharacterGroupAdmin() == false)
				throw new UserErrorMessage("Character is not a group admin");
			throw new UserErrorMessage("Unable to merge with specified group.");
		}
		
		// Key was set by the service, just save the entity.
		ds.put(charGroup);
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
	}

}
