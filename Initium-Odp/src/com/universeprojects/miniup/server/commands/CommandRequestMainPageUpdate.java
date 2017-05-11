package com.universeprojects.miniup.server.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.GamePageUpdateService;
import com.universeprojects.miniup.server.services.MainPageUpdateService;

public class CommandRequestMainPageUpdate extends Command
{

	public CommandRequestMainPageUpdate(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
	{
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException
	{
		boolean newUI = "true".equals(parameters.get("newUI"));
		String[] updateList = parameters.get("updateList").split(",");
		
		CachedEntity currentLocation = db.getEntity((Key)db.getCurrentCharacter().getProperty("locationKey"));
		
		MainPageUpdateService mpus = null;
		if (newUI)
			mpus = new GamePageUpdateService(db, db.getCurrentUser(), db.getCurrentCharacter(), currentLocation, this);
		else
			mpus = new MainPageUpdateService(db, db.getCurrentUser(), db.getCurrentCharacter(), currentLocation, this);
		
		// Use the given updateList to find the method we want to execute on the mpus
		for(String updateMethodName:updateList)
		{
			if(updateMethodName.startsWith("update")==false)
				throw new IllegalArgumentException("Method name doesn't start with 'update'.");
			
			Method method;
			try
			{
				method = mpus.getClass().getMethod(updateMethodName);
			
				method.invoke(mpus);
			}
			catch (NoSuchMethodException e)
			{
				throw new IllegalArgumentException("Unable to find the update method '"+updateMethodName+"' on the MPUS. Note: The update method must have no arguments.");
			}
			catch (SecurityException e)
			{
				throw new IllegalArgumentException(e);
			}
			catch (IllegalAccessException e)
			{
				throw new IllegalArgumentException(e);
			}
			catch (IllegalArgumentException e)
			{
				throw new IllegalArgumentException(e);
			}
			catch (InvocationTargetException e)
			{
				throw new IllegalArgumentException(e);
			}
		}
		
	}

}
