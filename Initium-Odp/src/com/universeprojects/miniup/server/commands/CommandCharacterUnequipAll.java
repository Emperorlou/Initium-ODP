package com.universeprojects.miniup.server.commands;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.universeprojects.cacheddatastore.CachedDatastoreService;
import com.universeprojects.cacheddatastore.CachedEntity;
import com.universeprojects.miniup.server.ODPDBAccess;
import com.universeprojects.miniup.server.commands.framework.Command;
import com.universeprojects.miniup.server.commands.framework.UserErrorMessage;

/**
 * Allows the player to attack a mob at the current location
 * 
 * Usage notes:
 * Checks if caller can attack and initiates combat if so
 * 
 * Parameters:
 * 		charId - characterId of monster to attack 
 * 
 * Support methods:
 * 		canAttack - returns boolean whether attack is allowed
 * 
 * @author SPFiredrake
 *
 */
public class CommandCharacterUnequipAll extends Command {

	public CommandCharacterUnequipAll(ODPDBAccess db, HttpServletRequest request, HttpServletResponse response) 
	{
		super(db, request, response);
	}
	
	@Override
	public void run(Map<String, String> parameters) throws UserErrorMessage {
		// TODO Auto-generated method stub
		ODPDBAccess db = getDB();
		CachedDatastoreService ds = getDS();
		CachedEntity character = db.getCurrentCharacter();
		
		for(String slot:ODPDBAccess.EQUIPMENT_SLOTS)
		{
			character.setProperty("equipment" + slot, null);
		}
		ds.put(character);
	}

}
