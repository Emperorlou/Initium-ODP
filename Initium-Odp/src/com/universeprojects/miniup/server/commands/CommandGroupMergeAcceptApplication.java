package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
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
		
		if(CommonChecks.checkCharacterIsZombie(character))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");

		Long groupID = parameters.containsKey("groupId") ? tryParseId(parameters, "groupId") : null;
		if(groupID == null) throw new RuntimeException("Command missing parameter groupId");
		
		CachedEntity groupToMerge = db.getEntity("Group", groupID);

		// Ensure the mergeGroup actually requested the merge
		if (GameUtils.equals(groupToMerge.getProperty("pendingMergeGroupKey"), character.getProperty("groupKey"))==false)
			throw new UserErrorMessage("The group you're attempting to merge did not indicate they wish to merge with your group.");
		
		GroupService service = new GroupService(db, character);

		if(service.characterHasGroup() == false)
			throw new UserErrorMessage("Character does not belong to a group!");
		if(service.isCharacterGroupCreator() == false)
			throw new UserErrorMessage("Character is not an admin of the group!");
		if(service.isCharacterInSpecifiedGroup(groupToMerge))
			throw new RuntimeException("Unable to merge identical groups!");
		
		if(service.acceptMergeApplicationFrom(ds, groupToMerge))
			setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
	}

}
