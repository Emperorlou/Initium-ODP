package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.commands.framework.Command.JavascriptResponse;
import com.universeprojects.miniup.server.services.GroupService;

public class CommandGroupAllianceRequest extends Command {

	public CommandGroupAllianceRequest(ODPDBAccess db, HttpServletRequest request,
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
		String groupName = parameters.get("groupName");
		if(character.getProperty("groupKey") == null)
			throw new UserErrorMessage("Character does not belong to a group!");
		CachedEntity group = db.getGroupByName(groupName);
		
		// Group refers to the group we will be merging with. GroupService handles security 
		// permissions, determining whether the character is an admin of his group.
		GroupService service = new GroupService(db, character);
		
		CachedEntity charGroup = service.setAllianceRequest(group);
		if(charGroup == null)
		{
			if(service.characterHasGroup() == false)
				throw new UserErrorMessage("Character does not belong to a group");
			if(service.isCharacterInSpecifiedGroup(group))
				throw new RuntimeException("Specified group is characters own group");
		}
		setPopupMessage("Request submitted successfully.");
		// Key was set by the service, just save the entity.
		ds.put(charGroup);
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
	}

}