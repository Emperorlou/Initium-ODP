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
		
		CachedEntity user = db.getCurrentUser();
		Key antiBotQuestionKey = (Key)user.getProperty("aniBotQuestionKey");
		if (antiBotQuestionKey==null)
			return;
		
		CachedEntity antiBotQuestion = db.getEntity(antiBotQuestionKey);
		if (antiBotQuestion==null)
		{
			user.setProperty("antiBotQuestionKey", null);
			db.getDB().put(user);
			return;
		}
		
		String realAnswer = (String)antiBotQuestion.getProperty("answer");
		realAnswer = realAnswer.toLowerCase();
		
		if (answer.equals(realAnswer))
		{
			// Correct!
			user.setProperty("antiBotQuestionKey", null);
			db.getDB().put(user);
		}
		else
		{
			throw new UserErrorMessage("This answer is incorrect.");
		}
	}


}
