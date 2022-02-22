package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.UserRequestIncompleteException;
import com.universeprojects.miniup.server.WebUtils;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.ItemFilterService;

public class CommandAddItemFilter extends Command{

	public CommandAddItemFilter(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage, UserRequestIncompleteException {
		//The user has an item in their possession and gives it to this.
		
		CachedEntity character = db.getCurrentCharacter();
		
		Long itemId = Long.parseLong(parameters.get("itemId"));
		CachedEntity item = db.getEntity("Item", itemId);
		
		if(item == null)
			throw new UserErrorMessage("This item doesn't exist. Interesting...");
		
		if(false == CommonChecks.checkItemIsAccessible(item, character))
			throw new UserErrorMessage("You can't reach this item.");
		
		ItemFilterService ifs = new ItemFilterService(db);
		
		ifs.addItemFilter(item);
		
		db.getDB().put(character);
		db.sendGameMessage("You now have a new item filter! " + 
		"<a onclick='loadItemFilterList()'>[View your active item filters]</a>" +
				" <a onclick='removeItemFilter(event,'" + WebUtils.jsSafe((String) item.getProperty("name")) + "')>[Remove this filter]</a>" +
		" <a onclick='removeAllItemFilters()'>[Remove all of " + character.getProperty("name") + "'s item filters]</a>");
	}

}
