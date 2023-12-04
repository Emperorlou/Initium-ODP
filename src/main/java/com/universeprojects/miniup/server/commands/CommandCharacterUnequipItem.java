package com.universeprojects.miniup.server.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
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
		
		if(CommonChecks.checkCharacterIsZombie(character))
			throw new UserErrorMessage("You can't control yourself... Must... Eat... Brains...");
		
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
		
		// Keep track of which slots the item is in, so we can update the necessary slots in equipment list.
		List<String> oldEquips = new ArrayList<String>();
		for(String slot:ODPDBAccess.EQUIPMENT_SLOTS)
			if(GameUtils.equals(character.getProperty("equipment"+slot), itemKey))
				oldEquips.add(slot);
		
		db.doCharacterUnequipEntity(ds, character, equipmentItem);
		
		ds.put(character);
		
		// If we've gotten this far, we can assume it was successful. Update the in banner widget.
		// JS function reloads the page popup.
		MainPageUpdateService mpus = MainPageUpdateService.getInstance(db, db.getCurrentUser(), character, null, this);
		mpus.updateInBannerCharacterWidget();
		
		// Now update the slots
		for(String slot:oldEquips)
			updateHtmlContents(".equip-item span[rel='" + slot + "']", "None");
		
		// Add the unequipped item to top of inventory list.
		prependChildHtml("#invItems", GameUtils.renderInventoryItem(db, equipmentItem, character, false));
	}

}
