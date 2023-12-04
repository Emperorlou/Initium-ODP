package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.services.ExperimentalPageUpdateService;
import com.universeprojects.miniup.server.services.MainPageUpdateService;

/**
 * Drops the specified item to the characters current location. Item must be in character's inventory.
 * 
 * @author SPFiredrake
 * 
 */
public class CommandCharacterDropItem extends Command {

	public CommandCharacterDropItem(ODPDBAccess db, HttpServletRequest request,
			HttpServletResponse response) {
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		ODPDBAccess db = ODPDBAccess.getInstance(request);
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();
		
		if(CommonChecks.checkCharacterIsZombie(character))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");
		
		Long itemId = tryParseId(parameters, "itemId");
		CachedEntity dropItem = db.getEntity("Item", itemId);
		if(dropItem == null) throw new RuntimeException("Item does not exist");
		if(GameUtils.equals(dropItem.getProperty("containerKey"), character.getKey()) == false)
			throw new UserErrorMessage("Item does not belong to character");
		if(CommonChecks.checkCharacterIsBusy(character))
			throw new UserErrorMessage("Your character is currently busy and cannot drop items.");
		
		db.doCharacterDropItem(character, dropItem, getSelectedTileX(), getSelectedTileY());
		
		// Should simply empty out the inventory item, save an unnecessary get.
		deleteHtml(".invItem[ref='"+itemId+"']");
		
		addJavascriptToResponse(db.getGridMapService().generateGridObjectJson(getSelectedTileX().intValue(), getSelectedTileY().intValue()));
	
	}

}
