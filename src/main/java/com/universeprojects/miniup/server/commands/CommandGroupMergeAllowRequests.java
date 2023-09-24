package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.HtmlComponents;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.ODPDBAccess.GroupStatus;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.GroupService;

public class CommandGroupMergeAllowRequests extends Command {

	public CommandGroupMergeAllowRequests(ODPDBAccess db, HttpServletRequest request,
			HttpServletResponse response) {
		super(db, request, response);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage 
	{
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();
		
		if(CommonChecks.checkCharacterIsZombie(character))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");

		GroupService service = new GroupService(db, character);
		CachedEntity charGroup = service.getCharacterGroup();
		
		if(service.setAllowMergeRequests(charGroup))
		{
			// property was updated by the service. Save group and update the HTML. 
			ds.put(charGroup);
			updateHtml("#mergeRequestAllow", "<a id='mergeRequestDisallow' onclick='groupMergeRequestsDisallow(event)'>Disallow Merge Requests</a>");
		}
		else
		{
			if(service.characterHasGroup()==false)
				throw new UserErrorMessage("Character does not belong to a group!");
			if(service.isCharacterGroupAdmin() == false)
				throw new UserErrorMessage("Character is not an admin of this group!");
			
			throw new UserErrorMessage("Unexpected error allowing merge requests! Please contact a dev.");
		}
	}

}
