package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

public class CommandTransferReceive extends Command{

	public CommandTransferReceive(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException {
		db.getDB().beginTransaction();
		
		String name = parameters.get("name");
		
		if(name == null)
			throw new UserErrorMessage("Character name cannot be blank");
		if (name.length()<1 || name.length()>30 || !name.matches("^[A-Za-z ]+[!]?$"))
			throw new UserErrorMessage("Character name must contain only letters and spaces, and must be between 1 and 30 characters long.");	
		
		CachedEntity user = db.getCurrentUser();
		
		user.setProperty("transferCharacterName", name);
		
		db.getDB().put(user);
		db.getDB().commit();
	}

}
