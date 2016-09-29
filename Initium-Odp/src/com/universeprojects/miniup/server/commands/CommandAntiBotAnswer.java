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
import com.universeprojects.miniup.server.services.AntiBotService;

public class CommandAntiBotAnswer extends Command
{

	public CommandAntiBotAnswer(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage
	{
		String answer = parameters.get("answer");
		if (answer==null)
			throw new UserErrorMessage("No answer given.");
		
		answer = answer.toLowerCase();
		
		AntiBotService antiBotService = new AntiBotService(db);
		CachedEntity antiBotQuestion = antiBotService.getAntiBotQuestion();
		
		if (antiBotQuestion==null)
		{
			antiBotService.clearAndSave();
			return;
		}
		
		String realAnswer = (String)antiBotQuestion.getProperty("answer");
		realAnswer = realAnswer.toLowerCase();
		
		if (answer.equals(realAnswer))
		{
			// Correct!
			antiBotService.clearAndSave();
			return;
		}
		else
		{
			throw new UserErrorMessage("This answer is incorrect.");
		}
	}


}
