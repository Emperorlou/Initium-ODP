package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.commands.framework.Command.JavascriptResponse;
import com.universeprojects.miniup.server.dbentities.GuardSetting;
import com.universeprojects.miniup.server.services.GuardService;

public class CommandGuardDeleteSetting extends Command
{

	public CommandGuardDeleteSetting(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException
	{
		Long guardSettingId = Long.parseLong(parameters.get("guardSettingId"));
		CachedEntity guardSettingRaw = db.getEntity("GuardSetting", guardSettingId);
		
		if (guardSettingRaw==null) return;
		
		GuardSetting guardSetting = new GuardSetting(db, guardSettingRaw);
		
		if (GameUtils.equals(guardSetting.getCharacterKey(), db.getCurrentCharacterKey())==false)
			throw new UserErrorMessage("You do not own this guard setting and cannot delete it. Try refreshing to see yours.");
		
		ds.delete(guardSettingRaw);
		
		setJavascriptResponse(JavascriptResponse.ReloadPagePopup);
	}

}
