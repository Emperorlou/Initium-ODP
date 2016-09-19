package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;


/**
 * This is a sample command you can use as a reference for creating new commands.
 * 
 * Usage notes:
 * Make sure to pass in an argument called 'test'. It is expected to have a value of 1 or 2, but you can
 * make it have any value (or leave it out) and it will actually show what it's like when an exception is thrown
 * on the server (which is also useful).
 * 
 * Parameters:
 * 		test (optional) - Value of 1 or 2 expected. Anything else throws an exception. 
 * 
 * @author Nik
 *
 */
public class CommandSample extends Command {

	public CommandSample(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) 
	{
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage 
	{
		// Get the DB access object. We use this for most DB communication. It is a place for reusable methods that access the DB.
		ODPDBAccess db = getDB();

		// Check if we're logged in or not. We shouldn't be able to see this since commands cannot be used unless the user is logged in.
		if (db.isLoggedIn(request)==false)
			throw new UserErrorMessage("You are not currently logged in and canont do this.");
		
		// We're going to use both the user and character entities of the logged in user
		// Needing at least the character is VERY common in commands since most commands
		// deal with the user who is activating them in one way or another
		CachedEntity user = db.getCurrentUser();
		CachedEntity character = db.getCurrentCharacter();

		// We're going to output whether or not we have a full account or a throwaway, just for fun
		boolean hasUser = false;
		if (user!=null) hasUser = true;
		
		// Grab the character name for output as well
		// IMPORTANT: The available properties on the character can be found in the editors!
		String characterName = (String)character.getProperty("name");
		
		// Lets get the parameter that was passed in, we'll do different things depending on what it is
		String test = parameters.get("test");
		
		// Add a test variable that we could use in the callback on the client (in the javascript)
		addCallbackData("testCallbackVariable", 100);
		
		// Now we'll poupup different messages (or throw exceptions) depending on what the 'test' parameter equals...
		if ("1".equals(test))
		{
			if (hasUser==true)
				setPopupMessage("The first test here is a message.<br>You are using a full account. Your character name is "+characterName+".");
			else
				setPopupMessage("The first test here is a message.<br>You are using a throwaway saccount. Your character name is "+characterName+".");
				
		}
		else if ("2".equals(test))
		{
			throw new UserErrorMessage("The second test here is an error message that halts execution of the command.");
		}
		else
		{
			throw new RuntimeException("Invalid 'test' value. The third test is a runtime exception which generates a server log of the error with a stack trace. The user also gets a message about it they can copy/paste to a dev for reference.");
		}
	}

}
