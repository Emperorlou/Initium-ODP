package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
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

	public CommandSample(CachedDatastoreService ds, HttpServletRequest request, HttpServletResponse response) 
	{
		super(ds, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage 
	{
		
		String test = parameters.get("test");
		if (test==null)
			test = "1";
		
		if (test.equals("1"))
		{
			setPopupMessage("The first test here is a message.");
		}
		else if (test.equals("2"))
		{
			throw new UserErrorMessage("The second test here is an error message that halts execution of the command.");
		}
		else
		{
			throw new RuntimeException("Invalid 'test' value. The third test is a runtime exception which generates a server log of the error with a stack trace. The user also gets a message about it they can copy/paste to a dev for reference.");
		}
	}

}
