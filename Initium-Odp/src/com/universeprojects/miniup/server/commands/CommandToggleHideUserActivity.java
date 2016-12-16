package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

/**
 * Toggle hide user activity command.
 * 
 * @author RevMuun
 * 
 */
public class CommandToggleHideUserActivity extends Command
{

	/**
	 * Command toggles cloaking, enabling or disabling based on current character entity state
	 * 
	 * @param request
	 *            Server request
	 * @param response
	 *            Server response
	 */
	public CommandToggleHideUserActivity(final ODPDBAccess db,
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
		CachedEntity user = db.getCurrentUser();
		
		//toggle the hide user activity status
		Boolean hideActivity = (Boolean) user.getProperty("hideUserActivity");
		
		//if hideActivity is null, that means the user has never toggled this. Thus, I am assuming
		//the display text read "Click here to hide" and setting it to true.
		if(hideActivity == null) 
			hideActivity = true;
		else
			hideActivity = !hideActivity;
		
		user.setProperty("hideUserActivity", hideActivity);
		
		ds.put(user);
		
		if(hideActivity)
			updateHtmlContents("#toggleHideUserActivity", "Click here to show your online status to your friends.");
		else
			updateHtmlContents("#toggleHideUserActivity", "Click here to hide your online status from your friends.");
		
		//temporary, remove after debug
		setPopupMessage("New hideActivity: " + hideActivity);
	}
}