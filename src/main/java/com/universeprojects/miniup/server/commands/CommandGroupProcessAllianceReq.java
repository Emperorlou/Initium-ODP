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

public class CommandGroupProcessAllianceReq extends Command {

	public CommandGroupProcessAllianceReq(ODPDBAccess db, HttpServletRequest request,
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
		
		String decision = parameters.get("decision");

		Long groupID = parameters.containsKey("groupId") ? tryParseId(parameters, "groupId") : null;
		if(groupID == null) throw new RuntimeException("Command missing parameter groupId");
		
		CachedEntity groupToAllyWith = db.getEntity("Group", groupID);

		// Ensure the request was actually made
		if (GameUtils.equals(groupToAllyWith.getProperty("pendingAllianceGroupKey"), character.getProperty("groupKey"))==false)
			throw new UserErrorMessage("The group you're attempting to ally with did not indicate they wish to ally with your group.");
		
		GroupService service = new GroupService(db, character);
		
		if (decision.equals("accept"))
		{
			if(service.acceptAllianceRequest(ds, groupToAllyWith))
				setPopupMessage("Request accepted.");
		}		
		else
		{
			if(service.declineAllianceRequest(ds, groupToAllyWith))
				setPopupMessage("Request declined.");
		}
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
	}
}

