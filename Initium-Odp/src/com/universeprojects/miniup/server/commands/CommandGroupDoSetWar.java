package com.universeprojects.miniup.server.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.commands.framework.Command.JavascriptResponse;
import com.universeprojects.miniup.server.services.GroupService;

/** 
 * 
 * @author poolrequest
 *
 */

public class CommandGroupDoSetWar extends Command {

	public CommandGroupDoSetWar(final ODPDBAccess db,
			final HttpServletRequest request, final HttpServletResponse response)
	{
		super(db, request, response);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final void run(final Map<String, String> parameters)
			throws UserErrorMessage
	{
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();
		
		if(CommonChecks.checkCharacterIsZombie(character))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");
		
		String decision = parameters.get("decision");
		
		GroupService service = new GroupService(db, character);
				
		if (decision.equals("begin"))
		{
			String groupName = parameters.get("groupName");
			CachedEntity group = db.getGroupByName(groupName);
			if (service.beginWar(ds, group))
				setPopupMessage("War has been declared!");	
		}
		else if (decision.equals("end"))
		{
			Long groupID = parameters.containsKey("groupId") ? tryParseId(parameters, "groupId") : null;
			if(groupID == null) throw new RuntimeException("Command missing parameter groupId");	
			CachedEntity group = db.getEntity("Group", groupID);
			if (service.endWar(ds, group))
				setPopupMessage("War has ended.");
		}						
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
	}
}
