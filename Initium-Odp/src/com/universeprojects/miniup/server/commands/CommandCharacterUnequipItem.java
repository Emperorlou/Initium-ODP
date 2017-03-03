package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.commands.framework.Command.JavascriptResponse;
import com.universeprojects.miniup.server.services.CombatService;
import com.universeprojects.miniup.server.services.MainPageUpdateService;

/**
 * Unequips the specified item from the character.
 * 
 * @author SPFiredrake
 *
 */
public class CommandCharacterUnequipItem extends Command {

	public CommandCharacterUnequipItem(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) 
	{
		super(db, request, response);
	}
	
	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		// TODO Auto-generated method stub
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();
		
		Long itemId = tryParseId(parameters, "itemId");
		Key itemKey = KeyFactory.createKey("Item", itemId);
		
		CachedEntity equipmentItem = db.getEntity(itemKey);
		if(equipmentItem == null)
			throw new UserErrorMessage("Item doesn't exist.");
		
		CombatService cs = new CombatService(db);
		if(cs.isInCombat(character))
			throw new UserErrorMessage("Cannot unequip items in combat!");
		
		if(CommonChecks.checkCharacterIsBusy(character))
			throw new UserErrorMessage("Your character is currently busy and cannot change equipment.");
		
		db.doCharacterUnequipEntity(ds, character, equipmentItem);
		
		// If we've gotten this far, we can assume it was successful. Update the in banner widget.
		// JS function reloads the page popup.
		MainPageUpdateService mpus = new MainPageUpdateService(db, db.getCurrentUser(), character, null, this);
		mpus.updateInBannerCharacterWidget();
	}

}
