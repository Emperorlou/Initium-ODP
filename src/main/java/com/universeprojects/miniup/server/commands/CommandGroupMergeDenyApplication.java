package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.HtmlComponents;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.GroupService;

/**
 * Allows group admins the ability to deny a merge request from another group.
 * Simply clears out the merge request group key on the requesting group entity.
 * @author spotupchik
 *
 */
public class CommandGroupMergeDenyApplication extends Command {

	public CommandGroupMergeDenyApplication(ODPDBAccess db, HttpServletRequest request,
			HttpServletResponse response) {
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();
		
		if(CommonChecks.checkCharacterIsZombie(character))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");

		Long groupID = parameters.containsKey("groupId") ? tryParseId(parameters, "groupId") : null;
		if(groupID == null) throw new RuntimeException("Command missing parameter groupId");
		
		CachedEntity group = db.getEntity("Group", groupID);
		GroupService service = new GroupService(db, character);
		if(service.characterHasGroup() == false)
			throw new UserErrorMessage("Character does not belong to a group!");
		if(service.isCharacterGroupCreator() == false)
			throw new UserErrorMessage("Character is not an admin of the group!");
		
		if(service.denyMergeApplicationFrom(group))
		{
			ds.put(group);
			updateHtmlContents(".group-container[ref='"+group.getId()+"'] .main-item-container", "Denied " + group.getProperty("name") + "'s Merge Request");
		}
	}

}
