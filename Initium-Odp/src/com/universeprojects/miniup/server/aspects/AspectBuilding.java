package com.universeprojects.miniup.server.aspects;


import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.InitiumAspect;
import com.universeprojects.miniup.server.InitiumObject;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class AspectBuilding extends InitiumAspect
{

	protected AspectBuilding(InitiumObject object)
	{
		super(object);
		
		// Add additional aspect requirements
		object.addAspect(AspectGridMapObject.class);
	}

	@Override
	public Integer getVersion() {
		return 1;
	}
	
	
	static
	{
		addCommand("BuildingEnter", AspectBuilding.CommandEnter.class);
	}
	

	public static class CommandEnter extends Command
	{

		public CommandEnter(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response)
		{
			super(db, request, response);
		}

		@Override
		public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException {
			Key buildingKey = KeyFactory.stringToKey(parameters.get("itemKey"));
			InitiumObject fireplace = db.getInitiumObject(buildingKey);

			if (fireplace.isAspectPresent(AspectBuilding.class) == false) throw new UserErrorMessage("The building you tried to enter was not, in fact, a building.");

			if (CommonChecks.checkItemIsAccessible(fireplace.getEntity(), db.getCurrentCharacter()) == false) throw new UserErrorMessage("You're not near this building.");
			
			
		}
	}	

}
