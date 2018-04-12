package com.universeprojects.miniup.server.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
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
		
		if(db.checkCharacterHasItemEquipped(character, item.getKey()))
			throw new UserErrorMessage("Item is already equipped.");
		
		// Keep track of old slots, so we can update the necessary slots in equipment list.
		Map<String,Key> oldEquips = new HashMap<String,Key>();
		for(String slot:ODPDBAccess.EQUIPMENT_SLOTS)
			oldEquips.put(slot, (Key)character.getProperty("equipment"+slot));
		
		// This method will throw relevant errors if it fails (not enough strength, no available slots, etc.) 
		db.doCharacterEquipEntity(ds, character, item);
		
		// If we've gotten this far, we can assume it was successful. Update the in banner widget.
		MainPageUpdateService mpus = MainPageUpdateService.getInstance(db, db.getCurrentUser(), character, null, this);
		mpus.updateInBannerCharacterWidget();
		
		// Remove the item from inventory first.
		deleteHtml("#inventory .invItem[ref='" + item.getId() + "']");
		
		// Update equipment slots next
		String equipItem = GameUtils.renderEquipSlot(item);
		List<Key> invItems = new ArrayList<Key>();
		for(String slot:ODPDBAccess.EQUIPMENT_SLOTS)
		{
			Key curSlot = (Key)character.getProperty("equipment"+slot);
			// If both slots are null, method checks null == null, which is true. 
			// Otherwise, something changed.
			if(GameUtils.equals(curSlot, oldEquips.get(slot))==false)
			{
				// curSlot will either be null (meaning something else caused it to unequip) or
				// the new item we equipped. If oldEquips[slot] is non-null, then we generate
				// the inventory entry for it. Keep a map of the inventory entries for every removed
				// item (so we don't generate more than one), and always update current slot.
				
				String newSlot = curSlot != null ? equipItem : "None";
				updateHtmlContents(".equip-item span[rel='" + slot + "']", newSlot);
				
				Key oldSlot = oldEquips.get(slot);
				if(oldSlot != null && invItems.contains(oldSlot)==false)
					invItems.add(oldSlot);
			}
		}
		
		// Finally, add any removed items back to inventory (at the top)
		if(invItems.isEmpty()==false)
		{
			StringBuilder sb = new StringBuilder();
			List<CachedEntity> eqItems = ds.get(invItems);
			for(CachedEntity curItem:eqItems)
				sb.append(GameUtils.renderInventoryItem(db, curItem, character, false));
			
			prependChildHtml("#invItems", sb.toString());
		}
	}
}
