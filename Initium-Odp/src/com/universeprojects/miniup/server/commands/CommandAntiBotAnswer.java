package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.WebUtils;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.CaptchaService;

public class CommandAntiBotAnswer extends Command
{

	public CommandAntiBotAnswer(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage
	{
		String answer = parameters.get("response");
		if (answer==null)
			throw new UserErrorMessage("No answer given.");
		
		if (db.validateCaptcha(answer, WebUtils.getClientIpAddr(request))==true)
		{
			CaptchaService service = new CaptchaService(db);
			service.flagCheckSucceeded();
		}
	}


}
