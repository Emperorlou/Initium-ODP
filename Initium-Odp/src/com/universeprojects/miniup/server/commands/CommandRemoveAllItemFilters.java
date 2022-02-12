package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.ItemFilterService;

public class CommandRemoveAllItemFilters extends Command{

	public CommandRemoveAllItemFilters(ODPDBAccess db, HttpServletRequest request,
									   HttpServletResponse response) {
		super(db, request, response);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException {		
		CachedEntity character = db.getCurrentCharacter();
		
		if(CommonChecks.checkCharacterIsZombie(character))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");
		
		if(CommonChecks.checkCharacterIsBusy(character))
			throw new UserErrorMessage("You're busy and can't do this.");
		
		
		ItemFilterService ifs = new ItemFilterService(db);
		ifs.removeAllFilters();
		
		db.getDB().put(character);
		db.sendGameMessage("You have removed all of your item filters.");
	}

}
