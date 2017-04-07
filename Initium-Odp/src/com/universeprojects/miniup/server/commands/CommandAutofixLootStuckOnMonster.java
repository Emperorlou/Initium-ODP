package com.universeprojects.miniup.server.commands;

import java.util.Date;
import java.util.Map;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Key;
import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.CommonChecks;
import com.universeprojects.miniup.server.GameUtils;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.WebUtils;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;
import com.universeprojects.miniup.server.commands.framework.Command.JavascriptResponse;

/**
 * Checks for monsters in the character's location that haven't dropped their non-natural loot. Moves
 * items to player's inventory if so. Only works in combat sites.
 * 
 * @author papamarsh
 * 
 */
public class CommandAutofixLootStuckOnMonster extends Command {

	public CommandAutofixLootStuckOnMonster(ODPDBAccess db, HttpServletRequest request,
			HttpServletResponse response) {
		super(db, request, response);
	}

	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		ODPDBAccess db = ODPDBAccess.getInstance(request);
		CachedDatastoreService ds = getDS();
		
		//grab the current character, the character key, and current location
		CachedEntity character = db.getCurrentCharacter();
		Key characterKey = character.getKey();
		CachedEntity location = db.getEntity((Key) character.getProperty("locationKey"));
		
		if(location==null)
		{
			//If a character with null locationKey uses the command, a refresh will send them to their hometown
			setJavascriptResponse(JavascriptResponse.FullPageRefresh);
			return;
		}
		
		//make sure that character is in a combat site, otherwise throw error
		if("CombatSite".equals(location.getProperty("type")))
		{
			throw new UserErrorMessage("This command only works in combat sites.");
		}
		
		//list of the characters at the location
		List<CachedEntity> charactersHere = db.getLocationCharacters(location.getKey());
		for(CachedEntity monster:charactersHere)
		{
			//check if character is NPC and Dead
			if (("NPC".equals(monster.getProperty("type")) && ("Dead".equals(monster.getProperty("mode")))))
			{
				//check each equipment slot
				for (String slot:ODPDBAccess.EQUIPMENT_SLOTS)
				{
					//make sure there's an item in this slot
					Key equipmentInSlotKey = (Key) character.getProperty("equipment" + slot);
					if (equipmentInSlotKey != null)
					{
						//if item is non-natural then move it to player's inventory
						CachedEntity equipmentInSlot = db.getEntity(equipmentInSlotKey);
						String isNatural = (String) equipmentInSlot.getProperty("naturalEquipment");
						if ("FALSE".equals(isNatural) || isNatural==null || "".equals(isNatural))
						{
							equipmentInSlot.setProperty("containerKey", characterKey);
							equipmentInSlot.setProperty("movedTimestamp", new Date());
							ds.put(equipmentInSlot);
						}
					}
				}
			}
		}
		setJavascriptResponse(JavascriptResponse.FullPageRefresh);
	}
}
