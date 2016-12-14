package com.universeprojects.miniup.server.commands;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.commands.framework.Command.JavascriptResponse;

/** 
 * 
 * @author poolrequest
 *
 */

public class CommandGroupDeclareWar extends Command {

	public CommandGroupDeclareWar(final ODPDBAccess db,
			final HttpServletRequest request, final HttpServletResponse response)
	{
		super(db, request, response);
	}
	
	@Override
	public final void run(final Map<String, String> parameters)
			throws UserErrorMessage
	{
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		String groupName = parameters.get("groupName");
		CachedEntity admin = db.getCurrentCharacter();		
		Key groupKey = (Key) admin.getProperty("groupKey");
		CachedEntity warDeclarer = db.getEntity(groupKey);
		CachedEntity warReceiver = db.getGroupByName(groupName);	
		
		if (warDeclarer == null)
		{
			throw new UserErrorMessage("You are not currently in a group.");
		}
		
		if (("Admin".equals(admin.getProperty("groupStatus"))) == false)
		{
			throw new UserErrorMessage(
					"You are not an admin of your group and cannot perform this action.");
		}
		
		if (warReceiver == null)
		{
			throw new UserErrorMessage(
					"Cannot declare war on a group that does not exist.");
		}
		
		List<Key> declarerCurrent = (List<Key>)warReceiver.getProperty("declaredWarGroups");
		if (declarerCurrent.contains(warReceiver.getKey()))
				{
					throw new UserErrorMessage(
							"War is already declared on this group.");
				}
		declarerCurrent.add(warReceiver.getKey());
		warDeclarer.setProperty("declaredWarGroups", declarerCurrent);
		
		ds.put(warDeclarer);
		
		setPopupMessage("Was has been declared!");
		
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);

	}
}
