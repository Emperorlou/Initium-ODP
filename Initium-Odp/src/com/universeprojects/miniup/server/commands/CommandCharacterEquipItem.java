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
import com.universeprojects.miniup.server.services.CombatService;
import com.universeprojects.miniup.server.services.MainPageUpdateService;

/**
 * Equips the specified item onto the character. Item must be in character's inventory.
 * 
 * @author SPFiredrake
 * 
 */
public class CommandCharacterEquipItem extends Command {

	public CommandCharacterEquipItem(ODPDBAccess db,
			HttpServletRequest request, HttpServletResponse response) {
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();
		
		Long itemId = tryParseId(parameters, "itemId");
		CachedEntity item = db.getEntity("Item", itemId);
		
		if(character == null) throw new RuntimeException("Character cannot be null");
		if(item == null) throw new UserErrorMessage("Item does not exist");
		if(GameUtils.equals(character.getKey(), item.getProperty("containerKey"))== false)
			throw new UserErrorMessage("Item does not belong to character");
		
		CombatService cs = new CombatService(db);
		if(cs.isInCombat(character)) 
			throw new UserErrorMessage("You cannot equip items while in combat!");
		
		if(CommonChecks.checkCharacterIsBusy(character))
			throw new UserErrorMessage("Your character is currently busy and cannot change equipment.");
		
		// This method will throw relevant errors if it fails (not enough strength, no available slots, etc.) 
		db.doCharacterEquipEntity(ds, character, item);
		
		// If we've gotten this far, we can assume it was successful. Update the in banner widget.
		// JS function reloads the page popup.
		MainPageUpdateService mpus = new MainPageUpdateService(db, db.getCurrentUser(), character, null, this);
		mpus.updateInBannerCharacterWidget();
	}

}
